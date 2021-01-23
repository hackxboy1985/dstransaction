

package org.saga.server.callback;


import org.saga.server.common.CompensateAckType;
import org.saga.server.txevent.TxEvent;

public interface OmegaCallback {
  void compensate(TxEvent event);

  default void disconnect() {
  }

  default void getAck(CompensateAckType type) {
  }

  default boolean isWaiting() {
    return false;
  }
}
