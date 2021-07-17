

package cn.ds.transaction.transfer.core;

import cn.ds.transaction.transfer.core.errorHandle.GrpcOnErrorHandler;
import cn.ds.transaction.transfer.core.errorHandle.PendingTaskRunner;
import io.grpc.ManagedChannel;
import java.util.Collection;
import java.util.Map;
import cn.ds.transaction.framework.interfaces.MessageSender;

/**
 * 负载均衡上下文，非常重要，组合了真实消息发送者、通道集合、任务执行器、错误处理器
 */
public class LoadBalanceContext {

    /**
     * 服务器多节点时会有多个连接，任何一个发送成功即可
     */
  private Map<MessageSender, Long> senders;

  private final Collection<ManagedChannel> channels;

  /**
   * 阻塞队列
   */
  private final PendingTaskRunner pendingTaskRunner;

  /**
   * 错误处理器
   */
  private final GrpcOnErrorHandler grpcOnErrorHandler;

  public LoadBalanceContext(Map<MessageSender, Long> senders,
      Collection<ManagedChannel> channels, int reconnectDelay, int timeoutSeconds) {
    this.senders = senders;
    this.channels = channels;

    //TODO:此处PendingTaskRunner是真正的异步线程执行者，而它的执行队列getPendingTasks()是个BlockingQueue，
    //TODO:这个Queue入队则由GrpcOnErrorHandler来完成，遇到grpc错误时，由
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
