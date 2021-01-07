

package cn.ds.transaction.transfer.core;

import cn.ds.transaction.transfer.core.errorHandle.GrpcOnErrorHandler;
import cn.ds.transaction.transfer.core.errorHandle.PendingTaskRunner;
import io.grpc.ManagedChannel;
import java.util.Collection;
import java.util.Map;
import cn.ds.transaction.framework.interfaces.MessageSender;

/**
 * 负载均衡上下文
 */
public class LoadBalanceContext {

    /**
     * 服务器多节点时会有多个连接，任何一个发送成功即可
     */
  private Map<MessageSender, Long> senders;

  private final Collection<ManagedChannel> channels;

  private final PendingTaskRunner pendingTaskRunner;

  private final GrpcOnErrorHandler grpcOnErrorHandler;

  public LoadBalanceContext(Map<MessageSender, Long> senders,
      Collection<ManagedChannel> channels, int reconnectDelay, int timeoutSeconds) {
    this.senders = senders;
    this.channels = channels;
    this.pendingTaskRunner = new PendingTaskRunner(reconnectDelay);
    this.grpcOnErrorHandler = new GrpcOnErrorHandler(pendingTaskRunner.getPendingTasks(), senders, timeoutSeconds);
    pendingTaskRunner.start();
  }

  public Map<MessageSender, Long> getSenders() {
    return senders;
  }

  public Collection<ManagedChannel> getChannels() {
    return channels;
  }

  public PendingTaskRunner getPendingTaskRunner() {
    return pendingTaskRunner;
  }

  public GrpcOnErrorHandler getGrpcOnErrorHandler() {
    return grpcOnErrorHandler;
  }

  // this is only for test
  public void setSenders(Map<MessageSender, Long> senders) {
    this.senders = senders;
  }
}
