

package cn.ds.transaction.framework;

import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.OmegaContext;
import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.exception.OmegaException;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.processor.SagaStartAnnotationProcessor;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import org.junit.Before;
import org.junit.Test;

import javax.transaction.TransactionalException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class SagaStartAnnotationProcessorTest {

  private final List<TxEvent> messages = new ArrayList<>();

  private final SagaMessageSender sender = new SagaMessageSender() {
    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public ServerMeta onGetServerMeta() {
      return null;
    }

    @Override
    public void close() {

    }

    @Override
    public String target() {
      return "UNKNOWN";
    }

    @Override
    public AlphaResponse send(TxEvent event) {
      messages.add(event);
      return new AlphaResponse(false);
    }
  };

  private final String globalTxId = UUID.randomUUID().toString();

  @SuppressWarnings("unchecked")
  private final IdGenerator<String> generator = mock(IdGenerator.class);
  private final OmegaContext context = new OmegaContext(generator);
  private final OmegaException exception = new OmegaException("exception",
      new RuntimeException("runtime exception"));

  private final SagaStartAnnotationProcessor sagaStartAnnotationProcessor = new SagaStartAnnotationProcessor(
      context,
      sender);

  @Before
  public void setUp() throws Exception {
    context.setGlobalTxId(globalTxId);
    context.setLocalTxId(globalTxId);
  }

  @Test
  public void sendsSagaStartedEvent() {
    sagaStartAnnotationProcessor.preIntercept(0);

    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.compensationMethod().isEmpty(), is(true));
    assertThat(event.type(), is(EventType.SagaStartedEvent));
    assertThat(event.payloads().length, is(0));
  }

  @Test
  public void sendsSagaEndedEvent() {
    sagaStartAnnotationProcessor.postIntercept(null);

    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.compensationMethod().isEmpty(), is(true));
    assertThat(event.type(), is(EventType.SagaEndedEvent));
    assertThat(event.payloads().length, is(0));
  }

  @Test
  public void transformInterceptedException() {
    SagaMessageSender sender = mock(SagaMessageSender.class);
    SagaStartAnnotationProcessor sagaStartAnnotationProcessor = new SagaStartAnnotationProcessor(
        context, sender);

    doThrow(exception).when(sender).send(any(TxEvent.class));

    try {
      sagaStartAnnotationProcessor.preIntercept(0);
      expectFailing(TransactionalException.class);
    } catch (TransactionalException e) {
      assertThat(e.getMessage(), is("exception"));
      assertThat(e.getCause(), instanceOf(RuntimeException.class));
      assertThat(e.getCause().getMessage(), is("runtime exception"));
    }
  }
}
