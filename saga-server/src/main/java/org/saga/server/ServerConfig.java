

package org.saga.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import cn.ds.transaction.grpc.protocol.ServerMeta;
import com.google.common.eventbus.EventBus;
import org.saga.server.callback.AgentCallback;
import org.saga.server.callback.PushBackAgentCallback;
import org.saga.server.command.CommandRepository;
import org.saga.server.callback.CompositeAgentCallback;
import org.saga.server.command.CommandEntityRepository;
import org.saga.server.common.NodeStatus;
import org.saga.server.command.SpringCommandRepository;
import org.saga.common.SagaServerMetaKeys;
import org.saga.server.txevent.SpringTxEventRepository;
import org.saga.server.txevent.TxConsistentService;
import org.saga.server.txevent.TxEventEnvelopeRepository;
import org.saga.server.txevent.TxEventRepository;
import org.saga.server.txtimeout.SpringTxTimeoutRepository;
import org.saga.server.txtimeout.TxTimeoutEntityRepository;
import org.saga.server.txtimeout.TxTimeoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.BindableService;

@EntityScan(basePackages = "org.saga.server")
@Configuration
public class ServerConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ServerConfig.class);
  private final BlockingQueue<Runnable> pendingCompensations = new LinkedBlockingQueue<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Value("${saga.compensation.retry.delay:3000}")
  private int delay;

  @Value("${saga.tx.timeout-seconds:600}")
  private int globalTxTimeoutSeconds;

  @Value("${saga.cluster.master.enabled:false}")
  private boolean masterEnabled;

  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  ApplicationEventPublisher applicationEventPublisher;


  @Bean("sagaEventBus")
  EventBus sagaEventBus() {
    return new EventBus("sagaEventBus");
  }

  @Bean
  Map<String, Map<String, AgentCallback>> agentCallbacks() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  AgentCallback agentCallback(Map<String, Map<String, AgentCallback>> callbacks) {
    return new PushBackAgentCallback(pendingCompensations, new CompositeAgentCallback(callbacks));
  }
  
  @Bean
  TxEventRepository springTxEventRepository(TxEventEnvelopeRepository eventRepo) {
    return new SpringTxEventRepository(eventRepo);
  }

  @Bean
  CommandRepository springCommandRepository(TxEventEnvelopeRepository eventRepo, CommandEntityRepository commandRepository) {
    return new SpringCommandRepository(eventRepo, commandRepository);
  }

  @Bean
  TxTimeoutRepository springTxTimeoutRepository(TxTimeoutEntityRepository timeoutRepo) {
    return new SpringTxTimeoutRepository(timeoutRepo);
  }

  @Bean
  ScheduledExecutorService compensationScheduler() {
    return scheduler;
  }

  @Bean
  NodeStatus nodeStatus (){
    if(masterEnabled){
      return new NodeStatus(NodeStatus.TypeEnum.SLAVE);
    }else{
      return new NodeStatus(NodeStatus.TypeEnum.MASTER);
    }
  }

  @Bean
  TxConsistentService txConsistentService(
      @Value("${saga.event.pollingInterval:500}") int eventPollingInterval,
      @Value("${saga.event.scanner.enabled:true}") boolean eventScannerEnabled,
      ScheduledExecutorService scheduler,
      TxEventRepository eventRepository,
      CommandRepository commandRepository,
      TxTimeoutRepository timeoutRepository,
      AgentCallback agentCallback,
      NodeStatus nodeStatus) {
        if (eventScannerEnabled) {
          new EventScanner(scheduler,
              eventRepository, commandRepository, timeoutRepository,
              agentCallback, eventPollingInterval, nodeStatus).run();
          LOG.info("Starting the EventScanner.");
          }
        TxConsistentService consistentService = new TxConsistentService(eventRepository);
        return consistentService;
  }

  /**
   * 缺省使用此配置 当没有saga.feature.akka.enabled配置时
   * @param serverConfig
   * @param txConsistentService
   * @param agentCallbacks
   * @param eventBus
   * @return
   * @throws IOException
   */
  @Bean()
  @ConditionalOnProperty(name = "saga.feature.akka.enabled", havingValue = "false", matchIfMissing = true)
  ServerStartable serverStartable(GrpcServerConfig serverConfig, TxConsistentService txConsistentService,
      Map<String, Map<String, AgentCallback>> agentCallbacks,
      @Qualifier("sagaEventBus") EventBus eventBus) throws IOException {
    ServerMeta serverMeta = ServerMeta.newBuilder()
        .putMeta(SagaServerMetaKeys.AkkaEnabled.name(), String.valueOf(false)).build();
    List<BindableService> bindableServices = new ArrayList();
    bindableServices.add(new GrpcTxEventEndpointImpl(txConsistentService, agentCallbacks, serverMeta));

    ServerStartable bootstrap = new GrpcStartable(serverConfig, eventBus,
        bindableServices.toArray(new BindableService[0]));
    new Thread(bootstrap::start).start();
    LOG.info("saga.feature.akka.enabled=false, starting the [Saga Service]");
    return bootstrap;
  }


  @PostConstruct
  void init() {
    //https://github.com/elastic/elasticsearch/issues/25741
    System.setProperty("es.set.netty.runtime.available.processors", "false");
    new PendingTaskRunner(pendingCompensations, delay).run();
  }

  @PreDestroy
  void shutdown() {
    scheduler.shutdownNow();
  }
}
