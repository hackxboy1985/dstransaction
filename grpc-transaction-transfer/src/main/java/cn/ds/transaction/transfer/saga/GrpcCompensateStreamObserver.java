

package cn.ds.transaction.transfer.saga;

import java.lang.invoke.MethodHandles;

import cn.ds.transaction.transfer.core.ReconnectStreamObserver;
import cn.ds.transaction.transfer.core.LoadBalanceContext;
import cn.ds.transaction.framework.interfaces.MessageDeserializer;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.MessageSender;
import cn.ds.transaction.grpc.protocol.GrpcCompensateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 补偿命令观察者
 */
class GrpcCompensateStreamObserver extends ReconnectStreamObserver<GrpcCompensateCommand> {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public GrpcCompensateStreamObserver(LoadBalanceContext loadContext,
      MessageSender messageSender,
      MessageHandler messageHandler, MessageDeserializer deserializer) {
    super(loadContext, messageSender);
    this.messageHandler = messageHandler;
    this.deserializer = deserializer;
  }

  private final MessageHandler messageHandler;
  private final MessageDeserializer deserializer;


  /**
   * Grpc框架的StreamObserver类的实现，是双向流的回调方法
   * @param command
   */
  @Override
  public void onNext(GrpcCompensateCommand command) {
    LOG.info("Saga-Transaction::Compensate:Received compensate command, global tx id: {}, local tx id: {}, compensation method: {}",
        command.getGlobalTxId(), command.getLocalTxId(), command.getCompensationMethod());

    messageHandler.onReceive(
        command.getGlobalTxId(),
        command.getLocalTxId(),
        command.getParentTxId().isEmpty() ? null : command.getParentTxId(),
        command.getCompensationMethod(),
        deserializer.deserialize(command.getPayloads().toByteArray()));
  }
}
