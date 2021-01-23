

package org.saga.server.cluster.master.provider.jdbc.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "master_lock")
public class MasterLock {

  @Id
  private String serviceName;

  private String instanceId;

  private Date expireTime;

  private Date lockedTime;

  public MasterLock() {

  }

  public MasterLock(
      String serviceName,
      String instanceId) {
    this.serviceName = serviceName;
    this.instanceId = instanceId;
  }

  public Date getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(Date expireTime) {
    this.expireTime = expireTime;
  }

  public Date getLockedTime() {
    return lockedTime;
  }

  public void setLockedTime(Date lockedTime) {
    this.lockedTime = lockedTime;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getInstanceId() {
    return instanceId;
  }
}
