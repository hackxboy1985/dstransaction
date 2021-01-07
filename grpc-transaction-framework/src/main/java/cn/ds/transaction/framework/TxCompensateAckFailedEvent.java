

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class TxCompensateAckFailedEvent extends TxEvent {
  public TxCompensateAckFailedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod, Throwable throwable) {
    super(EventType.TxCompensateAckFailedEvent, globalTxId, localTxId, parentTxId, compensationMethod, 0, "", 0,0 ,0 ,0, 0, stackTrace(throwable));
  }
}