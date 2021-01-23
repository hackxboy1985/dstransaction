

package org.saga.server.txevent;

import static org.saga.common.EventType.TxCompensatedEvent;

import java.util.List;
import java.util.Optional;

import org.saga.server.TxEvent;
import org.saga.server.TxEventEnvelopeRepository;
import org.springframework.data.domain.PageRequest;

public class SpringTxEventRepository implements TxEventRepository {
  private static final PageRequest SINGLE_TX_EVENT_REQUEST = PageRequest.of(0, 1);
  private final TxEventEnvelopeRepository eventRepo;

  public SpringTxEventRepository(TxEventEnvelopeRepository eventRepo) {
    this.eventRepo = eventRepo;
  }

  @Override
  public void save(TxEvent event) {
    eventRepo.save(event);
  }

  @Override
  public Optional<List<TxEvent>> findFirstAbortedGlobalTransaction() {
    return eventRepo.findFirstAbortedGlobalTxByType();
  }

  @Override
  public List<TxEvent> findTimeoutEvents() {
    return eventRepo.findTimeoutEvents(SINGLE_TX_EVENT_REQUEST);
  }

  @Override
  public Optional<TxEvent> findTxStartedEvent(String globalTxId, String localTxId) {
    return eventRepo.findFirstStartedEventByGlobalTxIdAndLocalTxId(globalTxId, localTxId);
  }

  @Override
  public List<TxEvent> findTransactions(String globalTxId, String type) {
    return eventRepo.findByEventGlobalTxIdAndEventType(globalTxId, type);
  }

  @Override
  public List<TxEvent> findFirstUncompensatedEventByIdGreaterThan(long id, String type) {
    return eventRepo.findFirstByTypeAndSurrogateIdGreaterThan(type, id, SINGLE_TX_EVENT_REQUEST);
  }

  @Override
  public Optional<TxEvent> findFirstCompensatedEventByIdGreaterThan(long id) {
    return eventRepo.findFirstByTypeAndSurrogateIdGreaterThan(TxCompensatedEvent.name(), id);
  }

  @Override
  public void deleteDuplicateEvents(String type) {
    eventRepo.findDuplicateEventsByType(type).forEach((txEvent) ->eventRepo.
            deleteBySurrogateId(txEvent.id()));
  }
}
