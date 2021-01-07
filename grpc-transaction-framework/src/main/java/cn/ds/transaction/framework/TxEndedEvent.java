

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class TxEndedEvent extends TxEvent {
  public TxEndedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod) {
    super(EventType.TxEndedEvent, globalTxId, localTxId, parentTxId, compensationMethod, 0, "", 0, 0, 0, 0, 0);
  }
}
