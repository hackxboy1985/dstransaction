

package cn.ds.transaction.transfer.core.errorHandle;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

public class PendingTaskRunner {

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private final BlockingQueue<Runnable> pendingTasks = new LinkedBlockingQueue<>();

  private final int reconnectDelay;

  public PendingTaskRunner(int reconnectDelay) {
    this.reconnectDelay = reconnectDelay;
  }

  public void start() {
    scheduler.scheduleWithFixedDelay(new Runnable() {
      @Override
      public void run() {
        try {
          pendingTasks.take().run();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }, 0, reconnectDelay, MILLISECONDS);
  }

  public void shutdown() {
    scheduler.shutdown();
  }

  public BlockingQueue<Runnable> getPendingTasks() {
    return pendingTasks;
  }

  public int getReconnectDelay() {
    return reconnectDelay;
  }
}
