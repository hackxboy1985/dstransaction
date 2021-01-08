

package cn.ds.transaction.transfer.core.errorHandle;

import com.google.common.base.Supplier;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import cn.ds.transaction.framework.interfaces.MessageSender;
import cn.ds.transaction.framework.exception.SagaException;

public class GrpcOnErrorHandler {

  private final BlockingQueue<Runnable> pendingTasks;

  private final Map<MessageSender, Long> senders;

  private final GrpcRetryContext grpcRetryContext;

  public GrpcOnErrorHandler(BlockingQueue<Runnable> pendingTasks,
      Map<MessageSender, Long> senders, int timeoutSeconds) {
    this.pendingTasks = pendingTasks;
    this.senders = senders;
    this.grpcRetryContext = new GrpcRetryContext(timeoutSeconds);
  }

  public void handle(MessageSender messageSender) {
    final Runnable runnable = new PushBackReconnectRunnable(
        messageSender,
        senders,
        pendingTasks,
        grpcRetryContext.getReconnectedSenders()
    );
    synchronized (pendingTasks) {
      if (!pendingTasks.contains(runnable)) {
        pendingTasks.offer(runnable);
      }
    }
  }

  public GrpcRetryContext getGrpcRetryContext() {
    return grpcRetryContext;
  }

  public static class GrpcRetryContext {

    private final int timeoutSeconds;

    private final BlockingQueue<MessageSender> reconnectedSenders = new LinkedBlockingQueue<>();

    private final Supplier<MessageSender> defaultMessageSender = new Supplier<MessageSender>() {
      @Override
      public MessageSender get() {
        try {
          MessageSender messageSender = reconnectedSenders.poll(timeoutSeconds, TimeUnit.SECONDS);
          if (null == messageSender) {
            throw new SagaException("Failed to get reconnected sender, all saga server is down.");
          }
          return messageSender;
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new SagaException("Failed to get reconnected sender", e);
        }
      }
    };

    public GrpcRetryContext(int timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
    }

    public BlockingQueue<MessageSender> getReconnectedSenders() {
      return reconnectedSenders;
    }

    public Supplier<MessageSender> getDefaultMessageSender() {
      return defaultMessageSender;
    }
  }
}
