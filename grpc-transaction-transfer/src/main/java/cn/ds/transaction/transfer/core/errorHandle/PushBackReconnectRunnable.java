

package cn.ds.transaction.transfer.core.errorHandle;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import cn.ds.transaction.framework.interfaces.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushBackReconnectRunnable implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final MessageSender messageSender;
  private final Map<MessageSender, Long> senders;

  private final BlockingQueue<Runnable> pendingTasks;

  private final BlockingQueue<MessageSender> connectedSenders;

  public PushBackReconnectRunnable(
      MessageSender messageSender,
      Map<MessageSender, Long> senders,
      BlockingQueue<Runnable> pendingTasks,
      BlockingQueue<MessageSender> connectedSenders) {
    this.messageSender = messageSender;
    this.senders = senders;
    this.pendingTasks = pendingTasks;
    this.connectedSenders = connectedSenders;
  }

  @Override
  public void run() {
    try {
      LOG.info("Saga-Transaction::Reconnect:Retry connecting to saga at {}", messageSender.target());
      messageSender.onDisconnected();
      messageSender.onConnected();
      senders.put(messageSender, 0L);
      connectedSenders.offer(messageSender);
      LOG.info("Saga-Transaction::Reconnect:Retry connecting to saga at {} is successful", messageSender.target());
    } catch (Exception e) {
      LOG.error("Saga-Transaction::Reconnect:Failed to reconnect to saga at {}", messageSender.target(), e);
      pendingTasks.offer(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PushBackReconnectRunnable that = (PushBackReconnectRunnable) o;
    return Objects.equals(messageSender, that.messageSender);
  }

  @Override
  public int hashCode() {
    return Objects.hash(messageSender);
  }
}
