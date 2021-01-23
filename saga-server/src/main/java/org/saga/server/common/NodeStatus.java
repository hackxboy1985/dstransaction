

package org.saga.server.common;

public class NodeStatus {
  private TypeEnum typeEnum = TypeEnum.SLAVE;

  public NodeStatus(TypeEnum typeNnum) {
    this.typeEnum = typeNnum;
  }

  public boolean isMaster() {
    return typeEnum == NodeStatus.TypeEnum.MASTER;
  }

  public void setTypeEnum(TypeEnum typeNnum) {
    this.typeEnum = typeNnum;
  }

  public TypeEnum getTypeEnum() {
    return typeEnum;
  }

  public enum TypeEnum {
    MASTER,
    SLAVE
  }
}
