

package org.saga.server;

import javax.persistence.*;
import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;

@Entity
@Table(name = "TxEvent")
public class TxEvent {
  @Transient
  public static final long MAX_TIMESTAMP = 253402214400000L; // 9999-12-31 00:00:00

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long surrogateId;

  private String serviceName;
  private String instanceId;
  private Date creationTime;
  private String globalTxId;
  private String localTxId;
  private String parentTxId;
  private String type;
  private String compensationMethod;
  private Date expiryTime;
  private String retryMethod;
  private int retries;
  private byte[] payloads;

  private TxEvent() {
  }

  public TxEvent(TxEvent event) {
    this(event.surrogateId,
        event.serviceName,
        event.instanceId,
        event.creationTime,
        event.globalTxId,
        event.localTxId,
        event.parentTxId,
        event.type,
        event.compensationMethod,
        event.expiryTime,
        event.retryMethod,
        event.retries,
        event.payloads);
  }

  public TxEvent(
      String serviceName,
      String instanceId,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String type,
      String compensationMethod,
      byte[] payloads) {
    this(serviceName, instanceId, new Date(), globalTxId, localTxId, parentTxId, type, compensationMethod, 0, "", 0,
        payloads);
  }

  public TxEvent(
      String serviceName,
      String instanceId,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String type,
      String compensationMethod,
      int timeout,
      String retryMethod,
      int retries,
      byte[] payloads) {
    this(null, serviceName, instanceId, new Date(), globalTxId, localTxId, parentTxId, type, compensationMethod, timeout,
        retryMethod, retries, payloads);
  }

  public TxEvent(
      String serviceName,
      String instanceId,
      Date creationTime,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String type,
      String compensationMethod,
      int timeout,
      String retryMethod,
      int retries,
      byte[] payloads) {
    this(null, serviceName, instanceId, creationTime, globalTxId, localTxId, parentTxId, type, compensationMethod,
        timeout, retryMethod, retries, payloads);
  }

  TxEvent(Long surrogateId,
      String serviceName,
      String instanceId,
      Date creationTime,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String type,
      String compensationMethod,
      int timeout,
      String retryMethod,
      int retries,
      byte[] payloads) {
    this(surrogateId, serviceName, instanceId, creationTime, globalTxId, localTxId, parentTxId, type,
        compensationMethod,
        timeout == 0 ? new Date(MAX_TIMESTAMP) : new Date(creationTime.getTime() + SECONDS.toMillis(timeout)),
        retryMethod,
        retries,
        payloads);
  }

  TxEvent(Long surrogateId,
      String serviceName,
      String instanceId,
      Date creationTime,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String type,
      String compensationMethod,
      Date expiryTime,
      String retryMethod,
      int retries,
      byte[] payloads) {
    this.surrogateId = surrogateId;
    this.serviceName = serviceName;
    this.instanceId = instanceId;
    this.creationTime = creationTime;
    this.globalTxId = globalTxId;
    this.localTxId = localTxId;
    this.parentTxId = parentTxId;
    this.type = type;
    this.compensationMethod = compensationMethod;
    this.expiryTime = expiryTime;
    this.retryMethod = retryMethod;
    this.retries = retries;
    this.payloads = payloads;
  }

  public String serviceName() {
    return serviceName;
  }

  public String instanceId() {
    return instanceId;
  }

  public Date creationTime() {
    return creationTime;
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

  public String type() {
    return type;
  }

  public String compensationMethod() {
    return compensationMethod;
  }

  public byte[] payloads() {
    return payloads;
  }

  public long id() {
    return surrogateId;
  }

  public Date expiryTime() {
    return expiryTime;
  }

  public String retryMethod() {
    return retryMethod;
  }

  public int retries() {
    return retries;
  }

  @Override
  public String toString() {
    return "TxEvent{" +
        "surrogateId=" + surrogateId +
        ", serviceName='" + serviceName + '\'' +
        ", instanceId='" + instanceId + '\'' +
        ", creationTime=" + creationTime +
        ", globalTxId='" + globalTxId + '\'' +
        ", localTxId='" + localTxId + '\'' +
        ", parentTxId='" + parentTxId + '\'' +
        ", type='" + type + '\'' +
        ", compensationMethod='" + compensationMethod + '\'' +
        ", expiryTime=" + expiryTime +
        ", retryMethod='" + retryMethod + '\'' +
        ", retries=" + retries +
        '}';
  }
}
