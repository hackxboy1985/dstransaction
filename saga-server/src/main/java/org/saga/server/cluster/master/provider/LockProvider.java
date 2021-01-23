

package org.saga.server.cluster.master.provider;

import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLock;

import java.util.Optional;

public interface LockProvider {
  Optional<Lock> lock(MasterLock masterLock);
}
