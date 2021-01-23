

package org.saga.server.cluster.master.provider.jdbc;

import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Optional;

@ConditionalOnProperty(name = "alpha.cluster.master.enabled", havingValue = "true")
public class SpringMasterLockRepository implements MasterLockRepository {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final MasterLockEntityRepository electionRepo;

  SpringMasterLockRepository(MasterLockEntityRepository electionRepo) {
    this.electionRepo = electionRepo;
  }

  @Override
  public boolean initLock(MasterLock masterLock) {
    try {
      Optional<MasterLock> lock = this.findMasterLockByServiceName(masterLock.getServiceName());
      if (!lock.isPresent()) {
        electionRepo.initLock(masterLock.getServiceName(), masterLock.getExpireTime(), masterLock.getLockedTime(), masterLock.getInstanceId());
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      LOG.error("Init lock error", e);
      return false;
    }
  }

  @Override
  public boolean updateLock(MasterLock masterLock) {
    try {
      int size = electionRepo.updateLock(
          masterLock.getServiceName(),
          new Date(),
          masterLock.getExpireTime(),
          masterLock.getInstanceId());
      return size > 0 ? true : false;
    } catch (Exception e) {
      LOG.error("Update lock error", e);
      return false;
    }
  }

  @Override
  public void unLock(String serviceName, Date expireTime) {
    electionRepo.unLock(serviceName, expireTime);
  }

  @Override
  public Optional<MasterLock> findMasterLockByServiceName(String serviceName) {
    return electionRepo.findMasterLockByServiceName(serviceName);
  }
}
