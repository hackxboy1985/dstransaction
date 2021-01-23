

package org.saga.server.cluster.master.provider;

import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;

import java.util.Optional;

public abstract class AbstractLockProvider implements LockProvider {

  private final LockProviderPersistence lockProviderPersistence;

  private boolean lockInitialization;

  protected AbstractLockProvider(LockProviderPersistence lockProviderPersistence) {
    this.lockProviderPersistence = lockProviderPersistence;
  }

  @Override
  public Optional<org.saga.server.cluster.master.provider.Lock> lock(MasterLock masterLock) {
    boolean lockObtained = doLock(masterLock);
    if (lockObtained) {
      return Optional.of(new Lock(this,masterLock, lockProviderPersistence));
    } else {
      return Optional.empty();
    }
  }

  protected boolean doLock(MasterLock masterLock) {
    if (!lockInitialization) {
      lockInitialization = true;
      if (lockProviderPersistence.initLock(masterLock)) {
        return true;
      }
    }
    return lockProviderPersistence.updateLock(masterLock);
  }

  public void doUnLock(){
    this.lockInitialization = false;
  }

  private static class Lock implements org.saga.server.cluster.master.provider.Lock {
    private final MasterLock masterLock;
    private final AbstractLockProvider provider;
    private final LockProviderPersistence lockProviderPersistence;

    Lock(AbstractLockProvider provider, MasterLock masterLock, LockProviderPersistence lockProviderPersistence) {
      this.provider = provider;
      this.masterLock = masterLock;
      this.lockProviderPersistence = lockProviderPersistence;
    }

    @Override
    public void unlock() {
      lockProviderPersistence.unLock(masterLock);
      provider.doUnLock();;
    }
  }
}
