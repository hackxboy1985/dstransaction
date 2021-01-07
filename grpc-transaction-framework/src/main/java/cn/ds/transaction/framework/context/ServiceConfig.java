

package cn.ds.transaction.framework.context;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServiceConfig {
  private final String serviceName;
  private final String instanceId;
  // Current DB only supports instance id less then 35
  private static final int MAX_LENGTH = 35;

  public ServiceConfig(String serviceName) {
    this(serviceName,null);
  }

  public ServiceConfig(String serviceName, String instanceId) {
    this.serviceName = serviceName;
    if(instanceId == null || "".equalsIgnoreCase(instanceId.trim())){
      try {
        this.instanceId = serviceName + "-" + InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
        throw new IllegalStateException(e);
      }
    }else{
      instanceId = instanceId.trim();
      this.instanceId = instanceId;
    }

    if (this.instanceId.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(String.format("The instanceId length exceeds maximum length limit [%d].", MAX_LENGTH));
    }
  }

  public String serviceName() {
    return serviceName;
  }

  public String instanceId() {
    return instanceId;
  }
}
