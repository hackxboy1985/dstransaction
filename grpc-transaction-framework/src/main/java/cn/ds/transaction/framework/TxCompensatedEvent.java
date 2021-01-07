

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class TxCompensatedEvent extends TxEvent {
  public TxCompensatedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod) {
    super(EventType.TxCompensatedEvent, globalTxId, localTxId, parentTxId, compensationMethod, 0, "", 0, 0, 0, 0, 0);
  }
}
