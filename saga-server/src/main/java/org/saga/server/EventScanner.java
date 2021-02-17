

package org.saga.server;

import org.saga.server.callback.AgentCallback;
import org.saga.server.command.Command;
import org.saga.server.command.CommandRepository;
import org.saga.server.common.NodeStatus;
import org.saga.server.common.TaskStatus;
import org.saga.server.txevent.TxEvent;
import org.saga.server.txevent.TxEventRepository;
import org.saga.server.txtimeout.TxTimeout;
import org.saga.server.txtimeout.TxTimeoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.saga.common.EventType.*;

public class EventScanner implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final byte[] EMPTY_PAYLOAD = new byte[0];

  private final ScheduledExecutorService scheduler;

  private final TxEventRepository eventRepository;

  private final CommandRepository commandRepository;

  private final TxTimeoutRepository timeoutRepository;

  private final AgentCallback agentCallback;

  private final int eventPollingInterval;

  private long nextEndedEventId;

  private long nextCompensatedEventId;

  private NodeStatus nodeStatus;

  public EventScanner(ScheduledExecutorService scheduler,
      TxEventRepository eventRepository,
      CommandRepository commandRepository,
      TxTimeoutRepository timeoutRepository,
      AgentCallback agentCallback,
      int eventPollingInterval,NodeStatus nodeStatus) {
    this.scheduler = scheduler;
    this.eventRepository = eventRepository;
    this.commandRepository = commandRepository;
    this.timeoutRepository = timeoutRepository;
    this.agentCallback = agentCallback;
//    this.eventPollingInterval = eventPollingInterval;
    this.eventPollingInterval = 10000;
    this.nodeStatus = nodeStatus;
  }

  @Override
  public void run() {
    try {
      // Need to catch the exception to keep the event scanner running.
      pollEvents();
    } catch (Exception ex) {
      LOG.warn("Got the exception {} when pollEvents.", ex.getMessage(), ex);
    }
  }

  private void pollEvents() {
    scheduler.scheduleWithFixedDelay(
        () -> {
          try {
            LOG.info("EventScanner schedule.");
            // only pull the events when working in the master mode
            if (nodeStatus.isMaster()) {
              updateTimeoutStatus();
              findTimeoutEvents();
              abortTimeoutEvents();
              saveUncompensatedEventsToCommands();
              compensate();
              updateCompensatedCommands();
              deleteDuplicateSagaEndedEvents();
              updateTransactionStatus();
            }
          }catch (Exception e){
            LOG.error("EventScanner scheduler error:{} ",e.getMessage(),e);
          }
        },
        3000,
        eventPollingInterval,
        MILLISECONDS);
  }

  private void findTimeoutEvents() {
    eventRepository.findTimeoutEvents()
        .forEach(event -> {
          LOG.info("Found timeout event {}", event);
          timeoutRepository.save(txTimeoutOf(event));
        });
  }

  private void updateTimeoutStatus() {
    timeoutRepository.markTimeoutAsDone();
  }

  /**
   * 将未补偿的事务保存至库command表
   */
  private void saveUncompensatedEventsToCommands() {
    eventRepository.findFirstUncompensatedEventByIdGreaterThan(nextEndedEventId, TxEndedEvent.name())
        .forEach(event -> {
          LOG.info("Found uncompensated event {}", event);
          nextEndedEventId = event.id();
          commandRepository.saveCompensationCommands(event.globalTxId());
        });
  }

  /**
   * 查询未完成的command补偿命令，调用实例进行补偿
   */
  private void compensate() {

    commandRepository.findFirstCommandToCompensate()
            .forEach(command -> {
              LOG.info("Compensating transaction with globalTxId {} and localTxId {}",
                      command.globalTxId(),
                      command.localTxId());

              agentCallback.compensate(txStartedEventOf(command));
            });
  }

  /**
   * 更新补偿过的子事务的命令表状态
   */
  private void updateCompensatedCommands() {
    eventRepository.findFirstCompensatedEventByIdGreaterThan(nextCompensatedEventId)
        .ifPresent(event -> {
          LOG.info("Found compensated event {}", event);
          nextCompensatedEventId = event.id();
          updateCompensationStatus(event);
        });
  }

  private void deleteDuplicateSagaEndedEvents() {
    try {
      eventRepository.deleteDuplicateEvents(SagaEndedEvent.name());
    } catch (Exception e) {
      LOG.warn("Failed to delete duplicate event", e);
    }
  }

  private void updateCompensationStatus(TxEvent event) {
    commandRepository.markCommandAsDone(event.globalTxId(), event.localTxId());
    LOG.info("Transaction with globalTxId {} and localTxId {} was compensated",
        event.globalTxId(),
        event.localTxId());

    markSagaEnded(event);
  }

  private void abortTimeoutEvents() {
    timeoutRepository.findFirstTimeout().forEach(timeout -> {
      LOG.info("Found timeout event {} to abort", timeout);

      eventRepository.save(toTxAbortedEvent(timeout));

      if (timeout.type().equals(TxStartedEvent.name())) {
        eventRepository.findTxStartedEvent(timeout.globalTxId(), timeout.localTxId())
            .ifPresent(agentCallback::compensate);
      }
    });
  }

  private void updateTransactionStatus() {
    eventRepository.findFirstAbortedGlobalTransaction().ifPresent(this::markGlobalTxEndWithEvents);
  }

  private void markSagaEnded(TxEvent event) {
    if (commandRepository.findUncompletedCommands(event.globalTxId()).isEmpty()) {
      markGlobalTxEndWithEvent(event);
    }
  }

  private void markGlobalTxEndWithEvent(TxEvent event) {
      TxEvent txEvent = toSagaEndedEvent(event);
      eventRepository.save(txEvent);
    LOG.info("Marked end of transaction with globalTxId {} : {}", event.globalTxId(), txEvent);
  }

  private void markGlobalTxEndWithEvents(List<TxEvent> events) {
    events.forEach(this::markGlobalTxEndWithEvent);
  }

  private TxEvent toTxAbortedEvent(TxTimeout timeout) {
    return new TxEvent(
        timeout.serviceName(),
        timeout.instanceId(),
        timeout.globalTxId(),
        timeout.localTxId(),
        timeout.parentTxId(),
        TxAbortedEvent.name(),
        "",
        ("Transaction timeout").getBytes());
  }

  private TxEvent toSagaEndedEvent(TxEvent event) {
    return new TxEvent(
        event.serviceName(),
        event.instanceId(),
        event.globalTxId(),
        event.globalTxId(),
        null,
        SagaEndedEvent.name(),
        "",
        EMPTY_PAYLOAD);
  }

  private TxEvent txStartedEventOf(Command command) {
    return new TxEvent(
        command.serviceName(),
        command.instanceId(),
        command.globalTxId(),
        command.localTxId(),
        command.parentTxId(),
        TxStartedEvent.name(),
        command.compensationMethod(),
        command.payloads());
  }

  private TxTimeout txTimeoutOf(TxEvent event) {
    return new TxTimeout(
        event.id(),
        event.serviceName(),
        event.instanceId(),
        event.globalTxId(),
        event.localTxId(),
        event.parentTxId(),
        event.type(),
        event.expiryTime(),
        TaskStatus.NEW.name());
  }
}
