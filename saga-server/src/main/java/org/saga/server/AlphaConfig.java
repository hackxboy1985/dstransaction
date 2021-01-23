

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
import org.saga.server.callback.OmegaCallback;
import org.saga.server.callback.PushBackOmegaCallback;
import org.saga.server.command.CommandRepository;
import org.saga.server.command.CompositeOmegaCallback;
import org.saga.server.common.CommandEntityRepository;
import org.saga.server.common.NodeStatus;
import org.saga.server.common.SpringCommandRepository;
import org.saga.common.AlphaMetaKeys;
import org.saga.server.txevent.SpringTxEventRepository;
import org.saga.server.txevent.TxConsistentService;
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

//@EntityScan(basePackages = "org.saga.server")
@EntityScan("org.saga.server")
@Configuration
public class AlphaConfig {
  private static final Logger LOG = LoggerFactory.getLogger(AlphaConfig.class);
  private final BlockingQueue<Runnable> pendingCompensations = new LinkedBlockingQueue<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Value("${alpha.compensation.retry.delay:3000}")
  private int delay;

  @Value("${alpha.tx.timeout-seconds:600}")
  private int globalTxTimeoutSeconds;

  @Value("${alpha.cluster.master.enabled:false}")
  private boolean masterEnabled;

  @Autowired
  ApplicationContext applicationContext;

  @Autowired
  ApplicationEventPublisher applicationEventPublisher;

//  @Bean(name="entityManagerFactory")
//  public LocalSessionFactoryBean sessionFactory() {
//    LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
//
//    return sessionFactory;
//  }

  @Bean("alphaEventBus")
  EventBus alphaEventBus() {
    return new EventBus("alphaEventBus");
  }

  @Bean
  Map<String, Map<String, OmegaCallback>> omegaCallbacks() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  OmegaCallback omegaCallback(Map<String, Map<String, OmegaCallback>> callbacks) {
    return new PushBackOmegaCallback(pendingCompensations, new CompositeOmegaCallback(callbacks));
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
      @Value("${alpha.event.pollingInterval:500}") int eventPollingInterval,
      @Value("${alpha.event.scanner.enabled:true}") boolean eventScannerEnabled,
      ScheduledExecutorService scheduler,
      TxEventRepository eventRepository,
      CommandRepository commandRepository,
      TxTimeoutRepository timeoutRepository,
      OmegaCallback omegaCallback,
      NodeStatus nodeStatus) {
        if (eventScannerEnabled) {
          new EventScanner(scheduler,
              eventRepository, commandRepository, timeoutRepository,
              omegaCallback, eventPollingInterval, nodeStatus).run();
          LOG.info("Starting the EventScanner.");
          }
        TxConsistentService consistentService = new TxConsistentService(eventRepository);
        return consistentService;
  }

  /**
   * 缺省使用此配置 当没有alpha.feature.akka.enabled配置时
   * @param serverConfig
   * @param txConsistentService
   * @param omegaCallbacks
   * @param eventBus
   * @return
   * @throws IOException
   */
  @Bean()
  @ConditionalOnProperty(name = "alpha.feature.akka.enabled", havingValue = "false", matchIfMissing = true)
  ServerStartable serverStartable(GrpcServerConfig serverConfig, TxConsistentService txConsistentService,
      Map<String, Map<String, OmegaCallback>> omegaCallbacks,
      @Qualifier("alphaEventBus") EventBus eventBus) throws IOException {
    ServerMeta serverMeta = ServerMeta.newBuilder()
        .putMeta(AlphaMetaKeys.AkkaEnabled.name(), String.valueOf(false)).build();
    List<BindableService> bindableServices = new ArrayList();
    bindableServices.add(new GrpcTxEventEndpointImpl(txConsistentService, omegaCallbacks, serverMeta));

    ServerStartable bootstrap = new GrpcStartable(serverConfig, eventBus,
        bindableServices.toArray(new BindableService[0]));
    new Thread(bootstrap::start).start();
    LOG.info("alpha.feature.akka.enabled=false, starting the saga db service");
    return bootstrap;
  }

//  @Bean
//  @ConditionalOnProperty(name= "alpha.feature.akka.enabled", havingValue = "true")
//  ServerStartable serverStartableWithAkka(GrpcServerConfig serverConfig,
//      Map<String, Map<String, OmegaCallback>> omegaCallbacks, @Autowired(required = false) GrpcTccEventService grpcTccEventService,
//      @Qualifier("alphaEventBus") EventBus eventBus, ActorEventChannel actorEventChannel) throws IOException {
//    ServerMeta serverMeta = ServerMeta.newBuilder()
//        .putMeta(AlphaMetaKeys.AkkaEnabled.name(), String.valueOf(true)).build();
//    List<BindableService> bindableServices = new ArrayList();
//    bindableServices.add(new GrpcSagaEventService(actorEventChannel, omegaCallbacks, serverMeta));
//    if (grpcTccEventService != null) {
//      LOG.info("alpha.feature.tcc.enable=true, starting the TCC service.");
//      bindableServices.add(grpcTccEventService);
//    } else {
//      LOG.info("alpha.feature.tcc.enable=false, the TCC service is disabled.");
//    }
//    ServerStartable bootstrap = new GrpcStartable(serverConfig, eventBus, bindableServices.toArray(new BindableService[0]));
//    new Thread(bootstrap::start).start();
//    LOG.info("alpha.feature.akka.enabled=true, starting the saga akka service.");
//    return bootstrap;
//  }

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
