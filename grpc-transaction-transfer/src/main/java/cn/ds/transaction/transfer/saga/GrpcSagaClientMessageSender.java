
package cn.ds.transaction.transfer.saga;


import cn.ds.transaction.framework.context.ServiceConfig;
import cn.ds.transaction.grpc.protocol.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import cn.ds.transaction.transfer.core.LoadBalanceContext;
import cn.ds.transaction.framework.SagaSvrResponse;
import cn.ds.transaction.framework.interfaces.MessageDeserializer;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.MessageSerializer;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.TxEvent;
import cn.ds.transaction.grpc.protocol.GrpcTxEvent.Builder;
import cn.ds.transaction.grpc.protocol.TxEventServiceGrpc.TxEventServiceBlockingStub;
//import cn.ds.transaction.grpc.protocol.TxEventServiceGrpc.TxEventServiceStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class GrpcSagaClientMessageSender implements SagaMessageSender {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String target;

  private final TxEventServiceGrpc.TxEventServiceStub asyncEventService;

  private final MessageSerializer serializer;

  private final TxEventServiceBlockingStub blockingEventService;

  private final GrpcCompensateStreamObserver compensateStreamObserver;

  private final GrpcServiceConfig serviceConfig;

  public GrpcSagaClientMessageSender(
      String address,
      ManagedChannel channel,
      MessageSerializer serializer,
      MessageDeserializer deserializer,
      ServiceConfig serviceConfig,
      MessageHandler handler,
      LoadBalanceContext loadContext) {
    this.target = address;
    //TODO:连接服务器异步的双向流，用于进行接收补偿消息，消息将会由GrpcCompensateStreamObserver接收处理
    this.asyncEventService = TxEventServiceGrpc.newStub(channel);
    //TODO:连接服务器同步单向流，阻塞发送消息直到返回结果，用于事务日志发送
    this.blockingEventService = TxEventServiceGrpc.newBlockingStub(channel);
    this.serializer = serializer;
    this.compensateStreamObserver =
        new GrpcCompensateStreamObserver(loadContext, this, handler, deserializer);
    this.serviceConfig = serviceConfig(serviceConfig.serviceName(), serviceConfig.instanceId());
  }

  /**
   *
   */
  @Override
  public void onConnected() {
    /**
     * 连接服务器，并向服务端发送serviceConfig即本服务的配置(包括serviceName/instanceId)，且设置消息回调为compensateStreamObserver
     */
    LOG.info("Saga-Transaction::GrpcSagaClientMessageSender:onConnected");
    asyncEventService.onConnected(compensateStreamObserver).onNext(serviceConfig);
  }

  @Override
  public void onDisconnected() {
    LOG.info("Saga-Transaction::GrpcSagaClientMessageSender:onDisconnected");
    blockingEventService.onDisconnected(serviceConfig);
  }

  @Override
  public ServerMeta onGetServerMeta() {
    LOG.info("Saga-Transaction::GrpcSagaClientMessageSender:getServerMeta");
    return blockingEventService.onGetServerMeta(serviceConfig);
  }

  @Override
  public void close() {
    // just do nothing here
  }

  @Override
  public String target() {
    return target;
  }

  @Override
  public SagaSvrResponse send(TxEvent event) {
    LOG.info("Saga-Transaction::Send {} to saga server:{}", event.type().name(),event);
    GrpcAck grpcAck = blockingEventService.onTxEvent(convertEvent(event));
    return new SagaSvrResponse(grpcAck.getAborted());
  }

  private GrpcTxEvent convertEvent(TxEvent event) {
    ByteString payloads = ByteString.copyFrom(serializer.serialize(event.payloads()));

    Builder builder = GrpcTxEvent.newBuilder()
        .setServiceName(serviceConfig.getServiceName())
        .setInstanceId(serviceConfig.getInstanceId())
        .setTimestamp(event.timestamp())
        .setGlobalTxId(event.globalTxId())
        .setLocalTxId(event.localTxId())
        .setParentTxId(event.parentTxId() == null ? "" : event.parentTxId())
        .setType(event.type().name())
        .setTimeout(event.timeout())
        .setForwardTimeout(event.forwardTimeout())
        .setReverseTimeout(event.reverseTimeout())
        .setCompensationMethod(event.compensationMethod())
        .setRetryMethod(event.retryMethod() == null ? "" : event.retryMethod())
        .setForwardRetries(event.forwardRetries())
        .setReverseRetries(event.reverseRetries())
        .setRetryDelayInMilliseconds(event.retryDelayInMilliseconds())
        .setPayloads(payloads);

    return builder.build();
  }

  private GrpcServiceConfig serviceConfig(String serviceName, String instanceId) {
    return GrpcServiceConfig.newBuilder()
        .setServiceName(serviceName)
        .setInstanceId(instanceId)
        .build();
  }
}
