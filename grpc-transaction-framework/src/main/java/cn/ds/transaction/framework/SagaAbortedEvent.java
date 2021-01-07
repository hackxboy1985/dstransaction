

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class SagaAbortedEvent extends TxEvent {

  public SagaAbortedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod, Throwable throwable) {
    super(EventType.SagaAbortedEvent, globalTxId, localTxId, parentTxId, compensationMethod, 0, "", 0,
        0, 0, 0, 0, stackTrace(throwable));
  }
}