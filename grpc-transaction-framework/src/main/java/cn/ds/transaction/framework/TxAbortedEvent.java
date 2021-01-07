

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class TxAbortedEvent extends TxEvent {

  public TxAbortedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod, Throwable throwable) {
    super(EventType.TxAbortedEvent, globalTxId, localTxId, parentTxId, compensationMethod, 0, "", 0,
        0, 0, 0, 0, stackTrace(throwable));
  }
}
