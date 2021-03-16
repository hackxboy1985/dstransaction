

package org.saga.server.txevent;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.saga.server.txevent.TxEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TxEventEnvelopeRepository extends CrudRepository<TxEvent, Long> {
  List<TxEvent> findByGlobalTxId(String globalTxId);

  //存在txaborted事件，但不存在txended或sagaended事件，且不存在该子事务的后续事件(localTxId相同但时间比aborted晚的事件),
  //且该子事务的started事件不是向前重试（补偿非重试）
  @Query("SELECT t FROM TxEvent t "
      + "WHERE t.type = 'TxAbortedEvent' AND NOT EXISTS( "
      + "  SELECT t1.globalTxId FROM TxEvent t1"
      + "  WHERE t1.globalTxId = t.globalTxId "
      + "    AND t1.type IN ('TxEndedEvent', 'SagaEndedEvent')) AND NOT EXISTS ( "
      + "  SELECT t3.globalTxId FROM TxEvent t3 "
      + "  WHERE t3.globalTxId = t.globalTxId "
      + "    AND t3.localTxId = t.localTxId "
      + "    AND t3.surrogateId != t.surrogateId "
      + "    AND t3.creationTime > t.creationTime) AND (("
      + "SELECT MIN(t2.retries) FROM TxEvent t2 "
      + "WHERE t2.globalTxId = t.globalTxId "
      + "  AND t2.localTxId = t.localTxId "
      + "  AND t2.type = 'TxStartedEvent') = 0 "
      + "OR t.globalTxId = t.localTxId)")
  Optional<List<TxEvent>> findFirstAbortedGlobalTxByType();

  @Query("SELECT t FROM TxEvent t "
      + "WHERE t.type IN ('TxStartedEvent', 'SagaStartedEvent') "
      + "  AND t.expiryTime < CURRENT_TIMESTAMP AND NOT EXISTS( "
      + "  SELECT t1.globalTxId FROM TxEvent t1 "
      + "  WHERE t1.globalTxId = t.globalTxId "
      + "    AND t1.localTxId = t.localTxId "
      + "    AND t1.type != t.type"
      + ")")
  List<TxEvent> findTimeoutEvents(Pageable pageable);

  @Query("SELECT t FROM TxEvent t "
      + "WHERE t.globalTxId = ?1 "
      + "  AND t.localTxId = ?2 "
      + "  AND t.type = 'TxStartedEvent'")
  Optional<TxEvent> findFirstStartedEventByGlobalTxIdAndLocalTxId(String globalTxId, String localTxId);

  @Query("SELECT DISTINCT new org.saga.server.txevent.TxEvent("
      + "t.serviceName, t.instanceId, t.globalTxId, t.localTxId, t.parentTxId, "
      + "t.type, t.compensationMethod, t.payloads "
      + ") FROM TxEvent t "
      + "WHERE t.globalTxId = ?1 AND t.type = ?2 "
      + "  AND ( SELECT MIN(t1.retries) FROM TxEvent t1 "
      + "  WHERE t1.globalTxId = t.globalTxId "
      + "    AND t1.localTxId = t.localTxId "
      + "    AND t1.type IN ('TxStartedEvent', 'SagaStartedEvent') ) = 0 ")
  List<TxEvent> findByEventGlobalTxIdAndEventType(String globalTxId, String type);

  //根据存在匹配的结束事件，但不存在补偿事件的条件查找开始事件
  @Query("SELECT t FROM TxEvent t "
      + "WHERE t.globalTxId = ?1 AND t.type = 'TxStartedEvent' AND EXISTS ( "
      + "  SELECT t1.globalTxId"
      + "  FROM TxEvent t1 "
      + "  WHERE t1.globalTxId = ?1 "
      + "  AND t1.localTxId = t.localTxId "
      + "  AND t1.type = 'TxEndedEvent'"
      + ") AND NOT EXISTS ( "
      + "  SELECT t2.globalTxId"
      + "  FROM TxEvent t2 "
      + "  WHERE t2.globalTxId = ?1 "
      + "  AND t2.localTxId = t.localTxId "
      + "  AND t2.type = 'TxCompensatedEvent') "
      + "ORDER BY t.surrogateId ASC")
  List<TxEvent> findStartedEventsWithMatchingEndedButNotCompensatedEvents(String globalTxId);

  @Query("SELECT t FROM TxEvent t "
      + "WHERE t.type = ?1 AND t.surrogateId > ?2 AND EXISTS ( "
      + "  SELECT t1.globalTxId FROM TxEvent t1 "
      + "  WHERE t1.globalTxId = t.globalTxId "
      + "    AND t1.type = 'TxAbortedEvent' AND NOT EXISTS ( "
      + "    SELECT t2.globalTxId FROM TxEvent t2 "
      + "    WHERE t2.globalTxId = t1.globalTxId "
      + "      AND t2.localTxId = t1.localTxId "
      + "      AND t2.type = 'TxStartedEvent' "
      + "      AND t2.creationTime > t1.creationTime)) AND NOT EXISTS ( "
      + "  SELECT t3.globalTxId FROM TxEvent t3 "
      + "  WHERE t3.globalTxId = t.globalTxId "
      + "    AND t3.localTxId = t.localTxId "
      + "    AND t3.type = 'TxCompensatedEvent') AND ( "
      + "  SELECT MIN(t4.retries) FROM TxEvent t4 "
      + "  WHERE t4.globalTxId = t.globalTxId "
      + "    AND t4.localTxId = t.localTxId "
      + "    AND t4.type = 'TxStartedEvent' ) = 0 "
      + "ORDER BY t.surrogateId ASC")
  List<TxEvent> findFirstByTypeAndSurrogateIdGreaterThan(String type, long surrogateId, Pageable pageable);

  Optional<TxEvent> findFirstByTypeAndSurrogateIdGreaterThan(String type, long surrogateId);

  @Query("SELECT t FROM TxEvent t "
      + "WHERE t.type = ?1 AND EXISTS ( "
      + "  SELECT t1.surrogateId"
      + "  FROM TxEvent t1 "
      + "  WHERE t1.globalTxId = t.globalTxId "
      + "  AND t1.localTxId = t.localTxId "
      + "  AND t1.type = t.type "
      + "  AND t1.surrogateId > t.surrogateId )")
  List<TxEvent> findDuplicateEventsByType(String type);

  List<TxEvent> findByServiceName(String serviceName);

  @Query("SELECT count(t) FROM TxEvent t" +
      " WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent'))")
  int findCountOfCompensatingEvents();

  @Query("SELECT t FROM TxEvent t" +
      " WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findCompensatingEvents();

  @Query("SELECT t FROM TxEvent t" +
      " WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findCompensatingEvents(Pageable pageable);

  @Query("SELECT count(t) FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent', 'TxCompensatedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent'))\n")
  int findCountOfCommittedEvents();


  @Query("SELECT t FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent', 'TxCompensatedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findCommittedEvents();

  @Query("SELECT t FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent', 'TxCompensatedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findCommittedEvents(Pageable pageable);

  @Query("SELECT count(t) FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId " +
      "FROM TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent'))\n")
  int findCountOfPendingEvents();


  @Query("SELECT t FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId " +
      "FROM TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findPendingEvents();

  @Query("SELECT t FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND NOT EXISTS(  SELECT t1.globalTxId " +
      "FROM TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findPendingEvents(Pageable pageable);

  @Query("SELECT count(t) FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND EXISTS(  SELECT t1.globalTxId " +
      "FROM TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM " +
      "TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxCompensatedEvent'))\n")
  int findCountOfRollBackedEvents();

  @Query("SELECT t FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND EXISTS(  SELECT t1.globalTxId " +
      "FROM TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM " +
      "TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxCompensatedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findRollBackedEvents();

  @Query("SELECT t FROM TxEvent t " +
      "WHERE t.type = 'SagaStartedEvent' " +
      "AND EXISTS(  SELECT t1.globalTxId " +
      "FROM TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxAbortedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM " +
      "TxEvent t1 WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('SagaEndedEvent')) " +
      "AND EXISTS(  SELECT t1.globalTxId FROM TxEvent t1 " +
      "WHERE t1.globalTxId = t.globalTxId  " +
      "AND t1.type IN ('TxCompensatedEvent')) " +
      "ORDER BY t.surrogateId DESC")
  List<TxEvent> findRollBackedEvents(Pageable pageable);

  @Query("SELECT count(DISTINCT t.globalTxId) FROM TxEvent t")
  int findTotalCountOfTransactions();

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM TxEvent WHERE surrogateId = ?1 ")
  void deleteBySurrogateId(Long surrogateId);
}
