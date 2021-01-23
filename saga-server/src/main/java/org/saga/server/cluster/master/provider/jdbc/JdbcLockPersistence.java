

package org.saga.server.cluster.master.provider.jdbc;

import org.saga.server.cluster.master.provider.LockProviderPersistence;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLockRepository;

class JdbcLockPersistence implements LockProviderPersistence {

  private final MasterLockRepository masterLockRepository;

  JdbcLockPersistence(MasterLockRepository masterLockRepository) {
    this.masterLockRepository = masterLockRepository;
  }

  public boolean initLock(MasterLock masterLock) {
    return this.masterLockRepository.initLock(masterLock);
  }

  public boolean updateLock(MasterLock masterLock) {
    return this.masterLockRepository.updateLock(masterLock);
  }

  public void unLock(MasterLock masterLock) {
    this.masterLockRepository.unLock(masterLock.getServiceName(),
        masterLock.getExpireTime());
  }
}
