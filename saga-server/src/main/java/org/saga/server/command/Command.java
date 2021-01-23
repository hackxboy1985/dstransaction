

package org.saga.server.command;

import org.saga.server.common.TaskStatus;
import org.saga.server.txevent.TxEvent;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Command")
public class Command {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long surrogateId;

  private long eventId;
  private String serviceName;
  private String instanceId;
  private String globalTxId;
  private String localTxId;
  private String parentTxId;
  private String compensationMethod;
  private byte[] payloads;
  private String status;

  private Date lastModified;

  @Version
  private long version;

  Command() {
  }

  private Command(long id,
      String serviceName,
      String instanceId,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String compensationMethod,
      byte[] payloads,
      String status) {

    this.eventId = id;
    this.serviceName = serviceName;
    this.instanceId = instanceId;
    this.globalTxId = globalTxId;
    this.localTxId = localTxId;
    this.parentTxId = parentTxId;
    this.compensationMethod = compensationMethod;
    this.payloads = payloads;
    this.status = status;
    this.lastModified = new Date();
  }

  public Command(long id,
      String serviceName,
      String instanceId,
      String globalTxId,
      String localTxId,
      String parentTxId,
      String compensationMethod,
      byte[] payloads) {

    this(id, serviceName, instanceId, globalTxId, localTxId, parentTxId, compensationMethod, payloads, TaskStatus.NEW.name());
  }

  public Command(TxEvent event) {
    this(event.id(),
        event.serviceName(),
        event.instanceId(),
        event.globalTxId(),
        event.localTxId(),
        event.parentTxId(),
        event.compensationMethod(),
        event.payloads());
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

  public String compensationMethod() {
    return compensationMethod;
  }

  public byte[] payloads() {
    return payloads;
  }

  String status() {
    return status;
  }

  long id() {
    return surrogateId;
  }

  @Override
  public String toString() {
    return "Command{" +
        "eventId=" + eventId +
        ", serviceName='" + serviceName + '\'' +
        ", instanceId='" + instanceId + '\'' +
        ", globalTxId='" + globalTxId + '\'' +
        ", localTxId='" + localTxId + '\'' +
        ", parentTxId='" + parentTxId + '\'' +
        ", compensationMethod='" + compensationMethod + '\'' +
        '}';
  }
}
