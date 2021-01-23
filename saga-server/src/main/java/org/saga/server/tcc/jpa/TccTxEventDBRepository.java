

package org.saga.server.tcc.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TccTxEventDBRepository extends CrudRepository<TccTxEvent, Long> {

  @Query(value = "SELECT t FROM TccTxEvent AS t WHERE t.globalTxId = ?1")
  Optional<List<TccTxEvent>> findByGlobalTxId(String globalTxId);

  @Query(value = "SELECT t FROM TccTxEvent AS t WHERE t.globalTxId = ?1 and t.localTxId = ?2 and t.txType = ?3")
  Optional<TccTxEvent> findByUniqueKey(String globalTxId, String localTxId, String txType);

  @Query(value = "SELECT t FROM TccTxEvent AS t WHERE t.globalTxId = ?1 and t.txType = ?2")
  Optional<List<TccTxEvent>> findByGlobalTxIdAndTxType(String globalTxId, String txType);
}
