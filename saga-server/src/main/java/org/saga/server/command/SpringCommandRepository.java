

package org.saga.server.command;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.saga.server.command.Command;
import org.saga.server.command.CommandEntityRepository;
import org.saga.server.command.CommandRepository;
import org.saga.server.common.TaskStatus;
import org.saga.server.txevent.TxEvent;
import org.saga.server.txevent.TxEventEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpringCommandRepository implements CommandRepository {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final TxEventEnvelopeRepository eventRepository;
  private final CommandEntityRepository commandRepository;

  public SpringCommandRepository(TxEventEnvelopeRepository eventRepository, CommandEntityRepository commandRepository) {
    this.eventRepository = eventRepository;
    this.commandRepository = commandRepository;
  }

  @Override
  public void saveCompensationCommands(String globalTxId) {
    //根据存在结束事件，但不存在补偿事件的条件查找出其开始事件TxStartedEvent
    List<TxEvent> events = eventRepository
        .findStartedEventsWithMatchingEndedButNotCompensatedEvents(globalTxId);

    Map<String, Command> commands = new LinkedHashMap<>();

    for (TxEvent event : events) {
      commands.computeIfAbsent(event.localTxId(), k -> new Command(event));
    }

    //TODO:将未补偿的落库
    for (Command command : commands.values()) {
      LOG.info("Save[Start] compensation command {}", command);
      try {
        commandRepository.save(command);
        LOG.info("Save[Succ] compensation command {}", command);
      } catch (Exception e) {
        LOG.warn("Save[Failed] compensation command to db {}", command);
      }
    }
  }

  @Override
  public void markCommandAsDone(String globalTxId, String localTxId) {
    commandRepository.updateStatusByGlobalTxIdAndLocalTxId(TaskStatus.DONE.name(), globalTxId, localTxId);
  }

  @Override
  public List<Command> findUncompletedCommands(String globalTxId) {
    return commandRepository.findByGlobalTxIdAndStatus(globalTxId, TaskStatus.NEW.name());
  }

  @Transactional
  @Override
  public List<Command> findFirstCommandToCompensate() {
    //TODO:查询所有未完成补偿的事务
    List<Command> commands = commandRepository
        .findFirstGroupByGlobalTxIdWithoutPendingOrderByIdDesc();

    commands.forEach(command ->
        commandRepository.updateStatusByGlobalTxIdAndLocalTxId(
            TaskStatus.NEW.name(),
            TaskStatus.PENDING.name(),
            command.globalTxId(),
            command.localTxId()));

    return commands;
  }
}
