

package cn.ds.transaction.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import cn.ds.transaction.framework.environment.Environment;
import cn.ds.transaction.framework.enums.EventType;

public class TxEvent {
  private final long timestamp;
  private final EventType type;
  private final String globalTxId;
  private final String localTxId;
  private final String parentTxId;
  private final String compensationMethod;
  private final int timeout;
  private final Object[] payloads;

  private final String retryMethod;
  private final int forwardRetries;
  private final int forwardTimeout;
  private final int reverseRetries;
  private final int reverseTimeout;
  private final int retryDelayInMilliseconds;

  public TxEvent(EventType type, String globalTxId, String localTxId, String parentTxId,
      String compensationMethod,
      int timeout, String retryMethod, int forwardRetries, int forwardTimeout, int reverseRetries,
      int reverseTimeout, int retryDelayInMilliseconds, Object... payloads) {
    this.timestamp = System.currentTimeMillis();
    this.type = type;
    this.globalTxId = globalTxId;
    this.localTxId = localTxId;
    this.parentTxId = parentTxId;
    this.compensationMethod = compensationMethod;
    this.timeout = timeout;
    this.retryMethod = retryMethod;
    this.forwardRetries = forwardRetries;
    this.forwardTimeout = forwardTimeout;
    this.reverseRetries = reverseRetries;
    this.reverseTimeout = reverseTimeout;
    this.retryDelayInMilliseconds = retryDelayInMilliseconds;
    this.payloads = payloads;
  }

  public long timestamp() {
    return timestamp;
  }

  public String globalTxId() {
    return globalTxId;
  }

  public String localTxId() {
    return localTxId;
  }

  public String parentTxId() {
    return parentTxId;
  }

  public Object[] payloads() {
    return payloads;
  }

  public EventType type() {
    return type;
  }

  public String compensationMethod() {
    return compensationMethod;
  }

  public int timeout() {
    return timeout;
  }

  public String retryMethod() {
    return retryMethod;
  }

  public int forwardRetries() {
    return forwardRetries;
  }

  public int forwardTimeout() {
    return forwardTimeout;
  }

  public int reverseRetries() {
    return reverseRetries;
  }

  public int reverseTimeout() {
    return reverseTimeout;
  }

  public int retryDelayInMilliseconds() {
    return retryDelayInMilliseconds;
  }

  @Override
  public String toString() {
    return type.name() + "{" +
        "globalTxId='" + globalTxId + '\'' +
        ", localTxId='" + localTxId + '\'' +
        ", parentTxId='" + parentTxId + '\'' +
        ", compensationMethod='" + compensationMethod + '\'' +
        ", timeout=" + timeout + '\'' +
        ", retryMethod='" + retryMethod + '\'' +
        ", forwardRetries=" + forwardRetries + '\'' +
        ", forwardTimeout=" + forwardTimeout + '\'' +
        ", reverseRetries=" + reverseRetries + '\'' +
        ", reverseTimeout=" + reverseTimeout + '\'' +
        ", payloads=" + Arrays.toString(payloads) +
        '}';
  }

  protected static String stackTrace(Throwable e) {
    StringWriter writer = new StringWriter();
    e.printStackTrace(new PrintWriter(writer));
    String stackTrace = writer.toString();
    if (stackTrace.length() > Environment.getInstance().getPayloadsMaxLength()) {
      stackTrace = stackTrace.substring(0, Environment.getInstance().getPayloadsMaxLength());
    }
    return stackTrace;
  }
}
