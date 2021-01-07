

package cn.ds.transaction.framework.wrapper;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutProbManager {

  private static TimeoutProbManager instance = new TimeoutProbManager(100);
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final transient Set<TimeoutProb> timeoutProbs = new ConcurrentSkipListSet<TimeoutProb>();
  private final transient ScheduledExecutorService interrupter =
      Executors.newSingleThreadScheduledExecutor(
          new TimeoutProbeThreadFactory()
      );

  public static TimeoutProbManager getInstance() {
    return instance;
  }

  public TimeoutProbManager(int delay) {
    this.interrupter.scheduleWithFixedDelay(
        new Runnable() {
          @Override
          public void run() {
            try {
              TimeoutProbManager.this.interrupt();
            } catch (Exception e) {
              LOG.error("The overtime thread interrupt fail", e);
            }
          }
        },
        0, delay, TimeUnit.MICROSECONDS
    );
  }

  public TimeoutProb addTimeoutProb(int timeout) {
    final TimeoutProb timeoutProb = new TimeoutProb(timeout);
    this.timeoutProbs.add(timeoutProb);
    return timeoutProb;
  }

  public void removeTimeoutProb(TimeoutProb timeoutProb) {
    this.timeoutProbs.remove(timeoutProb);
  }

  /**
   * Loop detection of all thread timeout probes, remove probe if the thread has terminated
   */
  private void interrupt() {
    synchronized (this.interrupter) {
      for (TimeoutProb timeoutProb : this.timeoutProbs) {
        if (timeoutProb.getInterruptFailureException() == null) {
          if (timeoutProb.expired()) {
            if (timeoutProb.interrupted()) {
              this.timeoutProbs.remove(timeoutProb);
            }
          }
        }
      }
    }
  }

  /**
   * Configuration timeout probe thread
   */
  public class TimeoutProbeThreadFactory implements ThreadFactory {

    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(new ThreadGroup("recovery-policy-timeout-wrapper"), runnable,
          "probe");
      thread.setPriority(Thread.MAX_PRIORITY);
      thread.setDaemon(true);
      return thread;
    }
  }
}
