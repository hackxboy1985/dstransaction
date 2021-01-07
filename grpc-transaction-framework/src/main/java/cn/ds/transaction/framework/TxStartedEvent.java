

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.enums.EventType;

public class TxStartedEvent extends TxEvent {

  public TxStartedEvent(String globalTxId, String localTxId, String parentTxId, String compensationMethod,
      int timeout, String retryMethod, int forwardRetries, int forwardTimeout, int reverseRetries, int reverseTimeout, int retryDelayInMilliseconds, Object... payloads) {
    super(EventType.TxStartedEvent, globalTxId, localTxId, parentTxId, compensationMethod, timeout, retryMethod,
        forwardRetries, forwardTimeout, reverseRetries, reverseTimeout, retryDelayInMilliseconds, payloads);
  }
}
