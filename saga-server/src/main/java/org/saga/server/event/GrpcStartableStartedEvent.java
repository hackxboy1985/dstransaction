

package org.saga.server.event;

public class GrpcStartableStartedEvent {
  private int port;
  public GrpcStartableStartedEvent(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }
}
