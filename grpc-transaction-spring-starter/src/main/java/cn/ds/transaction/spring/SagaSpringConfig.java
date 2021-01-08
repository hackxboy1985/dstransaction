

package cn.ds.transaction.spring;

import cn.ds.transaction.framework.CallbackContext;
import cn.ds.transaction.framework.common.SagaServerMetaKeys;
import cn.ds.transaction.framework.context.*;
import cn.ds.transaction.framework.interfaces.MessageFormat;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.messageFormat.KryoMessageFormat;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import cn.ds.transaction.transfer.SagaSvrClusterConfig;
import cn.ds.transaction.transfer.SagaSvrClusterDiscovery;
import cn.ds.transaction.transfer.core.FastestSender;
import cn.ds.transaction.transfer.core.LoadBalanceContext;
import cn.ds.transaction.transfer.core.LoadBalanceContextBuilder;
import cn.ds.transaction.transfer.core.TransactionType;
import cn.ds.transaction.transfer.saga.SagaLoadBalanceSender;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.lang.invoke.MethodHandles;

@Configuration
class SagaSpringConfig {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @ConditionalOnMissingBean
  @Bean(name = {"sagaUniqueIdGenerator"})
  IdGenerator<String> idGenerator() {
    return new UniqueIdGenerator();
  }

  @Bean
  SagaContext sagaContext(@Qualifier("sagaUniqueIdGenerator") IdGenerator<String> idGenerator, SagaMessageSender messageSender) {
    ServerMeta serverMeta = messageSender.onGetServerMeta();
    boolean akkaEnabeld = Boolean.parseBoolean(serverMeta.getMetaMap().get(SagaServerMetaKeys.AkkaEnabled.name()));
    return new SagaContext(idGenerator, SagaServerMetas.builder().akkaEnabled(akkaEnabeld).build());
  }

  @Bean(name = {"compensationContext"})
  CallbackContext compensationContext(SagaContext sagaContext, SagaMessageSender sender) {
    return new CallbackContext(sagaContext, sender);
  }


  @Bean
  ServiceConfig serviceConfig(@Value("${spring.application.name}") String serviceName, @Value("${saga.instance.instanceId:#{null}}") String instanceId) {
    return new ServiceConfig(serviceName,instanceId);
  }


  @Bean
  @ConditionalOnProperty(name = "saga.cluster.register.type", havingValue = "default", matchIfMissing = true)
  SagaSvrClusterDiscovery sagaSvrClusterDiscovery(@Value("${saga.cluster.address:0.0.0.0:8080}") String[] addresses){
    return SagaSvrClusterDiscovery.builder().addresses(addresses).build();
  }

  @Bean
  SagaSvrClusterConfig sagaSvrClusterConfig(
      @Value("${saga.cluster.ssl.enable:false}") boolean enableSSL,
      @Value("${saga.cluster.ssl.mutualAuth:false}") boolean mutualAuth,
      @Value("${saga.cluster.ssl.cert:client.crt}") String cert,
      @Value("${saga.cluster.ssl.key:client.pem}") String key,
      @Value("${saga.cluster.ssl.certChain:ca.crt}") String certChain,
      @Lazy SagaSvrClusterDiscovery sagaSvrClusterDiscovery,
      @Lazy MessageHandler handler
      ) {

    LOG.info("Saga-Transaction::Discovery saga sever cluster address {} from {}",sagaSvrClusterDiscovery.getAddresses() == null ? "" : String.join(",",sagaSvrClusterDiscovery.getAddresses()), sagaSvrClusterDiscovery.getDiscoveryType().name());
    MessageFormat messageFormat = new KryoMessageFormat();
    SagaSvrClusterConfig clusterConfig = SagaSvrClusterConfig.builder()
        .addresses(ImmutableList.copyOf(sagaSvrClusterDiscovery.getAddresses()))
        .enableSSL(enableSSL)
        .enableMutualAuth(mutualAuth)
        .cert(cert)
        .key(key)
        .certChain(certChain)
        .messageDeserializer(messageFormat)
        .messageSerializer(messageFormat)
        .messageHandler(handler)
        .build();
    return clusterConfig;
  }

  @Bean(name = "sagaLoadContext")
  LoadBalanceContext sagaLoadBalanceSenderContext(
      SagaSvrClusterConfig sagaSvrClusterConfig,
      ServiceConfig serviceConfig,
      @Value("${saga.connection.reconnectDelay:3000}") int reconnectDelay,
      @Value("${saga.connection.sending.timeout:8}") int timeoutSeconds) {
    LoadBalanceContext loadBalanceSenderContext = new LoadBalanceContextBuilder(
        TransactionType.SAGA,
            sagaSvrClusterConfig,
        serviceConfig,
        reconnectDelay,
        timeoutSeconds).build();
    return loadBalanceSenderContext;
  }

  @Bean
  SagaMessageSender sagaLoadBalanceSender(@Qualifier("sagaLoadContext") LoadBalanceContext loadBalanceSenderContext) {
    final SagaMessageSender sagaMessageSender = new SagaLoadBalanceSender(loadBalanceSenderContext, new FastestSender());
    sagaMessageSender.onConnected();
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        sagaMessageSender.onDisconnected();
        sagaMessageSender.close();
      }
    }));
    return sagaMessageSender;
  }

}
