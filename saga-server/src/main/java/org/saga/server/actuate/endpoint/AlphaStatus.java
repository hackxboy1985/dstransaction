

package org.saga.server.actuate.endpoint;


import org.saga.server.common.NodeStatus;

public class AlphaStatus {
  private NodeStatus.TypeEnum nodeType;

  public NodeStatus.TypeEnum getNodeType() {
    return nodeType;
  }

  public void setNodeType(NodeStatus.TypeEnum nodeType) {
    this.nodeType = nodeType;
  }
}
