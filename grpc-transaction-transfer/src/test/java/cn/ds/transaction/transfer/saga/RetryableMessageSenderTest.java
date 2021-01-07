

package cn.ds.transaction.transfer.saga;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cn.ds.transaction.framework.interfaces.MessageSender;
import cn.ds.transaction.framework.exception.OmegaException;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.SagaStartedEvent;
import cn.ds.transaction.framework.TxEvent;
import cn.ds.transaction.framework.TxStartedEvent;
import org.junit.Test;

public class RetryableMessageSenderTest {
  @SuppressWarnings("unchecked")
  private final BlockingQueue<MessageSender> availableMessageSenders = new LinkedBlockingQueue<>();
  private final SagaMessageSender messageSender = new RetryableMessageSender(availableMessageSenders);

  private final String globalTxId = uniquify("globalTxId");
  private final String localTxId = uniquify("localTxId");

  private final TxStartedEvent event = new TxStartedEvent(globalTxId, localTxId, null, "method x",
      0, null, 0, 0, 0, 0, 0);

  @Test
  public void sendEventWhenSenderIsAvailable() {
    SagaMessageSender sender = mock(SagaMessageSender.class);
    availableMessageSenders.add(sender);

    messageSender.send(event);

    verify(sender, times(1)).send(event);
  }

  @Test
  public void blowsUpWhenEventIsSagaStarted() {
    TxEvent event = new SagaStartedEvent(globalTxId, localTxId, 0);

    try {
      messageSender.send(event);
      expectFailing(OmegaException.class);
    } catch (OmegaException e) {
      assertThat(e.getMessage(),
          is("Failed to process subsequent requests because no alpha server is available"));
    }
  }

  @Test
  public void blowsUpWhenInterrupted() throws InterruptedException {
    Thread thread = new Thread( new Runnable() {
      @Override
      public void run() {
        try {
          messageSender.send(event);
          expectFailing(OmegaException.class);
        } catch (OmegaException e) {
          assertThat(e.getMessage().endsWith("interruption"), is(true));
        }
      }
    });

    thread.start();
    thread.interrupt();
    thread.join();
  }
}
