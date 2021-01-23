

package org.saga.server.metrics;


import org.saga.server.common.NodeStatus.TypeEnum;

public class AlphaMetrics {
  private MetricsBean metrics;
  private TypeEnum nodeType;

  public MetricsBean getMetrics() {
    return metrics;
  }

  public void setMetrics(MetricsBean metrics) {
    this.metrics = metrics;
  }

  public TypeEnum getNodeType() {
    return nodeType;
  }

  public void setNodeType(TypeEnum nodeType) {
    this.nodeType = nodeType;
  }
}
