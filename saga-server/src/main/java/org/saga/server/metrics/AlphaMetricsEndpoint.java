

package org.saga.server.metrics;

import org.saga.server.common.NodeStatus;
import org.saga.server.common.NodeStatus.TypeEnum;

import org.saga.server.common.NodeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AlphaMetricsEndpoint {

  @Autowired
  @Lazy
  private NodeStatus nodeStatus;

  @Autowired(required = false)
  MetricsService metricsService;

  public MetricsBean getMetrics() {
    return metricsService != null ? metricsService.metrics() : null;
  }

  public NodeStatus.TypeEnum getNodeType(){
    return nodeStatus.getTypeEnum();
  }

}
