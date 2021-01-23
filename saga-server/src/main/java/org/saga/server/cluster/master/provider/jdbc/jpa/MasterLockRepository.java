

package org.saga.server.cluster.master.provider.jdbc.jpa;

import java.util.Date;
import java.util.Optional;

public interface MasterLockRepository {

  boolean initLock(MasterLock masterLock);

  boolean updateLock(MasterLock masterLock);

  void unLock(String serviceName, Date expireTime);

  Optional<MasterLock> findMasterLockByServiceName(String serviceName);
}
