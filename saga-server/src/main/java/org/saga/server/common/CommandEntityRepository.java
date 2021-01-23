

package org.saga.server.common;

import java.util.List;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.saga.server.command.Command;
import org.saga.server.command.Command;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CommandEntityRepository extends CrudRepository<Command, Long> {

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE org.saga.server.command.Command c "
      + "SET c.status = :toStatus "
      + "WHERE c.globalTxId = :globalTxId "
      + "  AND c.localTxId = :localTxId "
      + "  AND c.status = :fromStatus")
  void updateStatusByGlobalTxIdAndLocalTxId(
      @Param("fromStatus") String fromStatus,
      @Param("toStatus") String toStatus,
      @Param("globalTxId") String globalTxId,
      @Param("localTxId") String localTxId);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE org.saga.server.command.Command c "
      + "SET c.status = :status "
      + "WHERE c.globalTxId = :globalTxId "
      + "  AND c.localTxId = :localTxId")
  void updateStatusByGlobalTxIdAndLocalTxId(
      @Param("status") String status,
      @Param("globalTxId") String globalTxId,
      @Param("localTxId") String localTxId);

  List<Command> findByGlobalTxIdAndStatus(String globalTxId, String status);

  // TODO: 2018/1/18 we assumed compensation will never fail. if all service instances are not reachable, we have to set up retry mechanism for pending commands
//  @Lock(LockModeType.OPTIMISTIC)//使用hibernate无法支持,要使用可换成eclipselink https://blog.csdn.net/weixin_41751625/article/details/107481271
  @Query(value = "SELECT * FROM Command AS c "
      + "WHERE c.eventId IN ("
      + " SELECT MAX(c1.eventId) FROM Command AS c1 "
      + " INNER JOIN Command AS c2 on c1.globalTxId = c2.globalTxId"
      + " WHERE c1.status = 'NEW' "
      + " GROUP BY c1.globalTxId "
      + " HAVING MAX( CASE c2.status WHEN 'PENDING' THEN 1 ELSE 0 END ) = 0) "
      + "ORDER BY c.eventId ASC LIMIT 1", nativeQuery = true)
  List<Command> findFirstGroupByGlobalTxIdWithoutPendingOrderByIdDesc();
}
