

package org.saga.server;

import cn.ds.transaction.grpc.protocol.*;
import cn.ds.transaction.grpc.protocol.TxEventServiceGrpc.TxEventServiceImplBase;
import io.grpc.stub.StreamObserver;
import org.saga.server.callback.GrpcAgentCallback;
import org.saga.server.callback.AgentCallback;
import org.saga.server.txevent.TxConsistentService;
import org.saga.server.txevent.TxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;

class GrpcTxEventEndpointImpl extends TxEventServiceImplBase {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final GrpcAck ALLOW = GrpcAck.newBuilder().setAborted(false).build();
  private static final GrpcAck REJECT = GrpcAck.newBuilder().setAborted(true).build();

  private final TxConsistentService txConsistentService;

  private final Map<String, Map<String, AgentCallback>> agentCallbacks;
  private final ServerMeta serverMeta;

  GrpcTxEventEndpointImpl(TxConsistentService txConsistentService,
                          Map<String, Map<String, AgentCallback>> agentCallbacks, ServerMeta serverMeta) {
    this.txConsistentService = txConsistentService;
    this.agentCallbacks = agentCallbacks;
    this.serverMeta = serverMeta;
  }

  @Override
  public StreamObserver<GrpcServiceConfig> onConnected(StreamObserver<GrpcCompensateCommand> responseObserver) {
    return new StreamObserver<GrpcServiceConfig>() {
      @Override
      public void onNext(GrpcServiceConfig grpcServiceConfig) {
        agentCallbacks
            .computeIfAbsent(grpcServiceConfig.getServiceName(), key -> new ConcurrentHashMap<>())
            .put(grpcServiceConfig.getInstanceId(), new GrpcAgentCallback(responseObserver));
      }

      @Override
      public void onError(Throwable throwable) {
        LOG.error(throwable.getMessage(), throwable);
      }

      @Override
      public void onCompleted() {
        LOG.info("Omega client called method onCompleted of GrpcServiceConfig");
      }
    };
  }

  // TODO: 2018/1/5 connect is async and disconnect is sync, meaning callback may not be registered on disconnected
  @Override
  public void onDisconnected(GrpcServiceConfig request, StreamObserver<GrpcAck> responseObserver) {
    AgentCallback callback = agentCallbacks.getOrDefault(request.getServiceName(), emptyMap())
        .remove(request.getInstanceId());

    if (callback != null) {
      callback.disconnect();
    }

    responseObserver.onNext(ALLOW);
    responseObserver.onCompleted();
  }

  /**
   * 保存事务cmd
   * @param message
   * @param responseObserver
   */
  @Override
  public void onTxEvent(GrpcTxEvent message, StreamObserver<GrpcAck> responseObserver) {
    boolean ok = txConsistentService.handle(new TxEvent(
        message.getServiceName(),
        message.getInstanceId(),
        new Date(),
        message.getGlobalTxId(),
        message.getLocalTxId(),
        message.getParentTxId().isEmpty() ? null : message.getParentTxId(),
        message.getType(),
        message.getCompensationMethod(),
        message.getTimeout(),
        message.getRetryMethod(),
        message.getForwardRetries(),
        message.getPayloads().toByteArray()
    ));

    responseObserver.onNext(ok ? ALLOW : REJECT);
    responseObserver.onCompleted();
  }

  @Override
  public void onGetServerMeta(GrpcServiceConfig request, StreamObserver<ServerMeta> responseObserver){
    responseObserver.onNext(this.serverMeta);
    responseObserver.onCompleted();
  }
}
