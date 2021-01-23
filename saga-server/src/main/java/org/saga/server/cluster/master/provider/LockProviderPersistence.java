

package org.saga.server.cluster.master.provider;

    import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;

public interface LockProviderPersistence {

  boolean initLock(MasterLock masterLock);

  boolean updateLock(MasterLock masterLock);

  void unLock(MasterLock masterLock);
}
