

package org.saga.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PendingTaskRunner {
  private final BlockingQueue<Runnable> pendingTasks;
  private final int delay;

  public PendingTaskRunner(BlockingQueue<Runnable> pendingTasks, int delay) {
    this.pendingTasks = pendingTasks;
    this.delay = delay;
  }

  public Future<?> run() {
    return Executors.newSingleThreadScheduledExecutor()
        .scheduleWithFixedDelay(() -> {
          try {
            pendingTasks.take().run();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }, 0, delay, MILLISECONDS);
  }
}
