

package org.saga.server.cluster.master.provider.jdbc;

import org.saga.server.cluster.master.provider.AbstractLockProvider;
import org.saga.server.cluster.master.provider.jdbc.jpa.MasterLockRepository;

public class JdbcLockProvider extends AbstractLockProvider {
  public JdbcLockProvider(MasterLockRepository masterLockRepository) {
    super(new JdbcLockPersistence(masterLockRepository));
  }
}
