

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class SagaStartedEvent extends TxEvent {
  public SagaStartedEvent(String globalTxId, String localTxId, int timeout) {
    // use "" instead of null as compensationMethod requires not null in sql
    super(EventType.SagaStartedEvent, globalTxId, localTxId, null, "", timeout, "", 0, 0, 0, 0, 0);
  }
}
