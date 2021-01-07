

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class SagaEndedEvent extends TxEvent {
  public SagaEndedEvent(String globalTxId, String localTxId) {
    super(EventType.SagaEndedEvent, globalTxId, localTxId, null, "", 0, "", 0, 0, 0, 0, 0);
  }
}
