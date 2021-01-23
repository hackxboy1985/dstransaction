

package org.saga.server.txtimeout;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TxTimeout")
public class TxTimeout {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long surrogateId;

  private long eventId;
  private String serviceName;
  private String instanceId;
  private String globalTxId;
  private String localTxId;
  private String parentTxId;
  private String type;
  private Date expiryTime;
  private String status;

  @Version
  private long version;

  TxTimeout() {
  }

  public TxTimeout(long eventId, String serviceName, String instanceId, String globalTxId, String localTxId,
                   String parentTxId, String type, Date expiryTime, String status) {
    this.eventId = eventId;
    this.serviceName = serviceName;
    this.instanceId = instanceId;
    this.globalTxId = globalTxId;
    this.localTxId = localTxId;
    this.parentTxId = parentTxId;
    this.type = type;
    this.expiryTime = expiryTime;
    this.status = status;
  }

  public String serviceName() {
    return serviceName;
  }

  public String instanceId() {
    return instanceId;
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

  public Date expiryTime() {
    return expiryTime;
  }

  public String status() {
    return status;
  }

  @Override
  public String toString() {
    return "TxTimeout{" +
        "eventId=" + eventId +
        ", serviceName='" + serviceName + '\'' +
        ", instanceId='" + instanceId + '\'' +
        ", globalTxId='" + globalTxId + '\'' +
        ", localTxId='" + localTxId + '\'' +
        ", parentTxId='" + parentTxId + '\'' +
        ", type='" + type + '\'' +
        ", expiryTime=" + expiryTime +
        ", status=" + status +
        '}';
  }
}
