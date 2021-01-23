

package org.saga.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

  private static final int DEFAULT_ALPHA_SERVER_PORT = 8080;

  @Value("${alpha.server.host:0.0.0.0}")
  private String host;

  @Value("${alpha.server.port:"+DEFAULT_ALPHA_SERVER_PORT+"}")
  private int port;

  @Value("${alpha.server.initialPort:"+DEFAULT_ALPHA_SERVER_PORT+"}")
  private int initialPort;

  @Value("${alpha.server.portAutoIncrement:true}")
  private boolean portAutoIncrement;

  @Value("${alpha.server.portCount:100}")
  private int portCount;

  @Value("${alpha.server.ssl.enable:false}")
  private boolean sslEnable;

  @Value("${alpha.server.ssl.cert:server.crt}")
  private String cert;

  @Value("${alpha.server.ssl.key:server.pem}")
  private String key;

  @Value("${alpha.server.ssl.mutualAuth:false}")
  private boolean mutualAuth;

  @Value("${alpha.server.ssl.clientCert:client.crt}")
  private String clientCert;

  @Value("${alpha.feature.nativetransport:false}")
  private boolean nativeTransport;

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getInitialPort() {
    return initialPort;
  }

  public boolean isPortAutoIncrement() {
    return portAutoIncrement;
  }

  public int getPortCount() {
    return portCount;
  }

  public boolean isSslEnable() {
    return sslEnable;
  }

  public String getCert() {
    return cert;
  }

  public String getKey() {
    return key;
  }

  public boolean isMutualAuth() {
    return mutualAuth;
  }

  public String getClientCert() {
    return clientCert;
  }

  public boolean isNativeTransport() {
    return nativeTransport;
  }
}


