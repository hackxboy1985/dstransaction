

package org.saga.server;

public interface ServerStartable {
  void start();
  // This method is used by the unit test
  GrpcServerConfig getGrpcServerConfig();
}
