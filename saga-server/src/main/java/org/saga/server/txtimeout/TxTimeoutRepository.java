

package org.saga.server.txtimeout;

import java.util.List;

public interface TxTimeoutRepository {
  void save(TxTimeout timeout);

  void markTimeoutAsDone();

  List<TxTimeout> findFirstTimeout();
}
