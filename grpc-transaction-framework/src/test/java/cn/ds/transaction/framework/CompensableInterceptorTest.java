

package cn.ds.transaction.framework;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CompensableInterceptorTest {

  private final List<TxEvent> messages = new ArrayList<>();
  private final String globalTxId = UUID.randomUUID().toString();
  private final String localTxId = UUID.randomUUID().toString();
  private final String parentTxId = UUID.randomUUID().toString();

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
    public SagaSvrResponse send(TxEvent event) {
      messages.add(event);
      return new SagaSvrResponse(false);
    }
  };
  
  private final String message = uniquify("message");

  private final String retryMethod = uniquify("retryMethod");
  private final String compensationMethod = getClass().getCanonicalName();

  @SuppressWarnings("unchecked")
  private final IdGenerator<String> idGenerator = Mockito.mock(IdGenerator.class);
  private final SagaContext context = new SagaContext(idGenerator);
  private final CompensableInterceptor interceptor = new CompensableInterceptor(context, sender);

  @Before
  public void setUp() throws Exception {
    context.setGlobalTxId(globalTxId);
    context.setLocalTxId(localTxId);
  }

  @Test
  public void sendsTxStartedEventBefore() throws Exception {
    int timeout = new Random().nextInt();
    int forwardRetries = new Random().nextInt();
    int forwardTimeout = new Random().nextInt();
    int reverseRetries = new Random().nextInt();
    int reverseTimeout = new Random().nextInt();
    int retryDelayInMilliseconds = new Random().nextInt();
    interceptor.preIntercept(parentTxId, compensationMethod, timeout, retryMethod, forwardRetries,
        forwardTimeout, reverseRetries, reverseTimeout, retryDelayInMilliseconds, message);

    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.forwardRetries(), is(forwardRetries));
    assertThat(event.forwardTimeout(), is(forwardTimeout));
    assertThat(event.reverseRetries(), is(reverseRetries));
    assertThat(event.reverseTimeout(), is(reverseTimeout));
    assertThat(event.timeout(), is(timeout));
    assertThat(event.retryDelayInMilliseconds(), is(retryDelayInMilliseconds));
    assertThat(event.retryMethod(), is(retryMethod));
    assertThat(event.type(), is(EventType.TxStartedEvent));
    assertThat(event.compensationMethod(), is(compensationMethod));
    assertThat(asList(event.payloads()).contains(message), is(true));
  }

  @Test
  public void sendsTxEndedEventAfter() throws Exception {
    interceptor.postIntercept(parentTxId, compensationMethod);

    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.type(), is(EventType.TxEndedEvent));
    assertThat(event.compensationMethod(), is(compensationMethod));
    assertThat(event.payloads().length, is(0));
  }

  @Test
  public void sendsTxAbortedEventOnError() throws Exception {
    interceptor.onError(parentTxId, compensationMethod, new RuntimeException("oops"));

    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.type(), is(EventType.TxAbortedEvent));
    assertThat(event.compensationMethod(), is(compensationMethod));
  }
}
