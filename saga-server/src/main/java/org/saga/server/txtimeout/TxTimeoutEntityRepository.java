

package org.saga.server.txtimeout;

import java.util.List;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.saga.server.txtimeout.TxTimeout;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TxTimeoutEntityRepository extends CrudRepository<TxTimeout, Long> {

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE org.saga.server.txtimeout.TxTimeout t "
      + "SET t.status = :status "
      + "WHERE t.globalTxId = :globalTxId "
      + "  AND t.localTxId = :localTxId")
  void updateStatusByGlobalTxIdAndLocalTxId(
      @Param("status") String status,
      @Param("globalTxId") String globalTxId,
      @Param("localTxId") String localTxId);

  @Lock(LockModeType.OPTIMISTIC)
  @Query("SELECT t FROM TxTimeout AS t "
      + "WHERE t.status = 'NEW' "
      + "  AND t.expiryTime < CURRENT_TIMESTAMP "
      + "ORDER BY t.expiryTime ASC")
  List<TxTimeout> findFirstTimeoutTxOrderByExpireTimeAsc(Pageable pageable);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE TxTimeout t "
      + "SET t.status = 'DONE' "
      + "WHERE t.status != 'DONE' AND EXISTS ("
      + "  SELECT t1.globalTxId FROM TxEvent t1 "
      + "  WHERE t1.globalTxId = t.globalTxId "
      + "    AND t1.localTxId = t.localTxId "
      + "    AND t1.type != t.type"
      + ")")
  void updateStatusOfFinishedTx();
}
