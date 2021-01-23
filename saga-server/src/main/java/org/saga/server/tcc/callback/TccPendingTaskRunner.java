

package org.saga.server.tcc.callback;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TccPendingTaskRunner {

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private final BlockingQueue<Runnable> pendingTasks = new LinkedBlockingQueue<>();

  private final int delay;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public TccPendingTaskRunner(int delay) {
    this.delay = delay;
  }

  public void start() {
    scheduler.scheduleWithFixedDelay(() -> {
      try {
        pendingTasks.take().run();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (Exception e) {
        LOG.error(e.getMessage());
      }
    }, 0, delay, MILLISECONDS);
  }

  public void shutdown() {
    scheduler.shutdown();
  }

  public BlockingQueue<Runnable> getPendingTasks() {
    return pendingTasks;
  }
}
