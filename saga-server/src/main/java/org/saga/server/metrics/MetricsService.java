

package org.saga.server.metrics;

public class MetricsService {

  private final MetricsBean metrics = new MetricsBean();

  public MetricsBean metrics() {
    return metrics;
  }

}
