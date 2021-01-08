

package cn.ds.transaction.transfer;

import cn.ds.transaction.framework.interfaces.MessageDeserializer;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.MessageSerializer;

import java.util.Collections;
import java.util.List;

public class SagaSvrClusterConfig {

  private List<String> addresses;

  private boolean enableSSL;

  private boolean enableMutualAuth;

  private String cert;

  private String key;

  private String certChain;

  private MessageSerializer messageSerializer;

  private MessageDeserializer messageDeserializer;

  private MessageHandler messageHandler;


  /**
   * @deprecated Use {@link Builder} instead.
   */
  @Deprecated
  public SagaSvrClusterConfig(List<String> addresses,
                              boolean enableSSL,
                              boolean enableMutualAuth,
                              String cert,
                              String key,
                              String certChain) {
    this.addresses = addresses == null ? Collections.<String>emptyList() : addresses;
    this.enableMutualAuth = enableMutualAuth;
    this.enableSSL = enableSSL;
    this.cert = cert;
    this.key = key;
    this.certChain = certChain;
  }

  private SagaSvrClusterConfig(List<String> addresses,
                               boolean enableSSL,
                               boolean enableMutualAuth,
                               String cert, String key, String certChain,
                               MessageSerializer messageSerializer,
                               MessageDeserializer messageDeserializer,
                               MessageHandler messageHandler) {
    this.addresses = addresses;
    this.enableSSL = enableSSL;
    this.enableMutualAuth = enableMutualAuth;
    this.cert = cert;
    this.key = key;
    this.certChain = certChain;
    this.messageSerializer = messageSerializer;
    this.messageDeserializer = messageDeserializer;
    this.messageHandler = messageHandler;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private List<String> addresses;
    private boolean enableSSL;
    private boolean enableMutualAuth;
    private String cert;
    private String key;
    private String certChain;
    private MessageSerializer messageSerializer;
    private MessageDeserializer messageDeserializer;
    private MessageHandler messageHandler;

    public Builder addresses(List<String> addresses) {
      this.addresses = addresses;
      return this;
    }

    public Builder enableSSL(boolean enableSSL) {
      this.enableSSL = enableSSL;
      return this;
    }

    public Builder enableMutualAuth(boolean enableMutualAuth) {
      this.enableMutualAuth = enableMutualAuth;
      return this;
    }

    public Builder cert(String cert) {
      this.cert = cert;
      return this;
    }

    public Builder key(String key) {
      this.key = key;
      return this;
    }

    public Builder certChain(String certChain) {
      this.certChain = certChain;
      return this;
    }

    public Builder messageSerializer(MessageSerializer messageSerializer) {
      this.messageSerializer = messageSerializer;
      return this;
    }

    public Builder messageDeserializer(MessageDeserializer messageDeserializer) {
      this.messageDeserializer = messageDeserializer;
      return this;
    }

    public Builder messageHandler(MessageHandler messageHandler) {
      this.messageHandler = messageHandler;
      return this;
    }



    public SagaSvrClusterConfig build() {
      return new SagaSvrClusterConfig(this.addresses,
          this.enableSSL,
          this.enableMutualAuth,
          this.cert,
          this.key,
          this.certChain,
          this.messageSerializer,
          this.messageDeserializer,
          messageHandler);
    }
  }

  public List<String> getAddresses() {
    return addresses;
  }

  public boolean isEnableSSL() {
    return enableSSL;
  }

  public boolean isEnableMutualAuth() {
    return enableMutualAuth;
  }

  public String getCert() {
    return cert;
  }

  public String getKey() {
    return key;
  }

  public String getCertChain() {
    return certChain;
  }

  public MessageSerializer getMessageSerializer() {
    return messageSerializer;
  }

  public MessageDeserializer getMessageDeserializer() {
    return messageDeserializer;
  }

  public MessageHandler getMessageHandler() {
    return messageHandler;
  }
}
