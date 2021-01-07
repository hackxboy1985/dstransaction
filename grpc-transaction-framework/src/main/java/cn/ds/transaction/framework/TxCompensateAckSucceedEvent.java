

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class TxCompensateAckSucceedEvent extends TxEvent {
  public TxCompensateAckSucceedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod) {
    super(EventType.TxCompensateAckSucceedEvent, globalTxId, localTxId, parentTxId, compensationMethod, 0, "", 0, 0, 0, 0, 0);
  }
}