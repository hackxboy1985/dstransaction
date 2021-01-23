

package org.saga.server.tcc.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.saga.server.tcc.jpa.GlobalTxEvent;
import org.saga.server.tcc.jpa.ParticipatedEvent;
import org.saga.server.tcc.jpa.TccTxEvent;
import org.saga.server.tcc.jpa.TccTxType;
import org.springframework.data.domain.Pageable;

public interface TccTxEventRepository {

  void saveGlobalTxEvent(GlobalTxEvent event);

  void saveParticipatedEvent(ParticipatedEvent event);

  void updateParticipatedEventStatus(ParticipatedEvent event);

  void coordinated(TccTxEvent event);

  void save(TccTxEvent event);

  Optional<List<TccTxEvent>> findByGlobalTxId(String globalTxId);

  Optional<List<ParticipatedEvent>> findParticipatedByGlobalTxId(String globalTxId);

  Optional<List<TccTxEvent>> findByGlobalTxIdAndTxType(String globalTxId, TccTxType tccTxType);

  Optional<TccTxEvent> findByUniqueKey(String globalTxId, String localTxId, TccTxType tccTxType);

  Optional<List<GlobalTxEvent>> findTimeoutGlobalTx(Date deadLine, String txType, Pageable pageable);

  void clearCompletedGlobalTx(Pageable pageable);

  Iterable<TccTxEvent> findAll();

  void deleteAll();

}
