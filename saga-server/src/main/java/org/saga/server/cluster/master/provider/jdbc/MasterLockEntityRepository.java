

package org.saga.server.cluster.master.provider.jdbc;

import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

import java.util.Date;
import java.util.Optional;

public interface MasterLockEntityRepository extends CrudRepository<MasterLock, String> {

  Optional<MasterLock> findMasterLockByServiceName(String serviceName);

  @Transactional
  @Modifying
  @Query(value = "INSERT INTO master_lock "
      + "(serviceName, expireTime, lockedTime, instanceId) "
      + "VALUES "
      + "(?1, ?2, ?3, ?4)", nativeQuery = true)
  int initLock(
      @Param("serviceName") String serviceName,
      @Param("expireTime") Date expireTime,
      @Param("lockedTime") Date lockedTime,
      @Param("instanceId") String instanceId);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock t "
      + "SET t.expireTime = :expireTime"
      + ",t.lockedTime = :lockedTime "
      + ",t.instanceId = :instanceId "
      + "WHERE t.serviceName = :serviceName AND (t.expireTime <= :lockedTime OR t.instanceId = :instanceId)")
  int updateLock(
      @Param("serviceName") String serviceName,
      @Param("lockedTime") Date lockedTime,
      @Param("expireTime") Date expireTime,
      @Param("instanceId") String instanceId);

  @Transactional
  @Modifying(clearAutomatically = true)
  @Query("UPDATE org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock t "
      + "SET t.expireTime = :expireTime "
      + "WHERE t.serviceName = :serviceName")
  int unLock(@Param("serviceName") String serviceName,
      @Param("expireTime") Date expireTime);
}
