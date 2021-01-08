
package cn.ds.transaction.framework;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.transaction.InvalidTransactionException;

import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.annotations.Compensable;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.recovery.DefaultRecovery;
import cn.ds.transaction.framework.recovery.RecoveryPolicy;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;

public class DefaultRecoveryTest {
  private final List<TxEvent> messages = new ArrayList<>();

  private final String globalTxId = UUID.randomUUID().toString();

  private final String localTxId = UUID.randomUUID().toString();

  private final String parentTxId = UUID.randomUUID().toString();

  private final String newLocalTxId = UUID.randomUUID().toString();

  private final RuntimeException oops = new RuntimeException("oops");

  @SuppressWarnings("unchecked")
  private final IdGenerator<String> idGenerator = mock(IdGenerator.class);

  private final SagaContext sagaContext = new SagaContext(idGenerator);

  private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

  private final MethodSignature methodSignature = mock(MethodSignature.class);

  private final Compensable compensable = mock(Compensable.class);

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

  private final CompensableInterceptor interceptor = new CompensableInterceptor(sagaContext, sender);

  private final RecoveryPolicy recoveryPolicy = new DefaultRecovery();

  @Before
  public void setUp() throws Exception {
    when(idGenerator.nextId()).thenReturn(newLocalTxId);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(joinPoint.getTarget()).thenReturn(this);

    when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("doNothing"));
    when(compensable.compensationMethod()).thenReturn("doNothing");
    when(compensable.forwardRetries()).thenReturn(0);

    sagaContext.setGlobalTxId(globalTxId);
    sagaContext.setLocalTxId(localTxId);
  }

  @Test
  public void recordEndedEventWhenSuccess() throws Throwable {
    when(joinPoint.proceed()).thenReturn(null);
    recoveryPolicy.apply(joinPoint, compensable, interceptor, sagaContext, parentTxId, 0);

    assertThat(messages.size(), is(2));

    TxEvent startedEvent = messages.get(0);
    assertThat(startedEvent.globalTxId(), is(globalTxId));
    assertThat(startedEvent.localTxId(), is(localTxId));
    assertThat(startedEvent.parentTxId(), is(parentTxId));
    assertThat(startedEvent.type(), is(EventType.TxStartedEvent));
    assertThat(startedEvent.forwardRetries(), is(0));
    assertThat(startedEvent.retryMethod(), is(""));

    TxEvent endedEvent = messages.get(1);
    assertThat(endedEvent.globalTxId(), is(globalTxId));
    assertThat(endedEvent.localTxId(), is(localTxId));
    assertThat(endedEvent.parentTxId(), is(parentTxId));
    assertThat(endedEvent.type(), is(EventType.TxEndedEvent));
  }

  @Test
  public void recordAbortedEventWhenFailed() throws Throwable {
    when(joinPoint.proceed()).thenThrow(oops);

    try {
      recoveryPolicy.apply(joinPoint, compensable, interceptor, sagaContext, parentTxId, 0);
      expectFailing(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getMessage(), is("oops"));
    }

    assertThat(messages.size(), is(2));

    TxEvent startedEvent = messages.get(0);
    assertThat(startedEvent.globalTxId(), is(globalTxId));
    assertThat(startedEvent.localTxId(), is(localTxId));
    assertThat(startedEvent.parentTxId(), is(parentTxId));
    assertThat(startedEvent.type(), is(EventType.TxStartedEvent));
    assertThat(startedEvent.forwardRetries(), is(0));
    assertThat(startedEvent.retryMethod(), is(""));

    TxEvent abortedEvent = messages.get(1);
    assertThat(abortedEvent.globalTxId(), is(globalTxId));
    assertThat(abortedEvent.localTxId(), is(localTxId));
    assertThat(abortedEvent.parentTxId(), is(parentTxId));
    assertThat(abortedEvent.type(), is(EventType.TxAbortedEvent));
  }

  @Test
  public void returnImmediatelyWhenReceivedRejectResponse() {
    SagaMessageSender sender = mock(SagaMessageSender.class);
    when(sender.send(any(TxEvent.class))).thenReturn(new SagaSvrResponse(true));

    CompensableInterceptor interceptor = new CompensableInterceptor(sagaContext, sender);

    try {
      recoveryPolicy.apply(joinPoint, compensable, interceptor, sagaContext, parentTxId, 0);
      expectFailing(InvalidTransactionException.class);
    } catch (InvalidTransactionException e) {
      assertThat(e.getMessage().contains("Abort sub transaction"), is(true));
    } catch (Throwable throwable) {
      fail("unexpected exception throw: " + throwable);
    }

    verify(sender, times(1)).send(any(TxEvent.class));
  }

  @Test
  public void recordRetryMethodWhenRetriesIsSet() throws Throwable {
    int retries = new Random().nextInt(Integer.MAX_VALUE - 1) + 1;
    when(compensable.forwardRetries()).thenReturn(retries);

    recoveryPolicy.apply(joinPoint, compensable, interceptor, sagaContext, parentTxId, retries);

    TxEvent startedEvent = messages.get(0);

    assertThat(startedEvent.globalTxId(), is(globalTxId));
    assertThat(startedEvent.localTxId(), is(localTxId));
    assertThat(startedEvent.parentTxId(), is(parentTxId));
    assertThat(startedEvent.type(), is(EventType.TxStartedEvent));
    assertThat(startedEvent.forwardRetries(), is(retries));
    assertThat(startedEvent.retryMethod(), is(this.getClass().getDeclaredMethod("doNothing").toString()));
  }

  private String doNothing() {
    return "doNothing";
  }
}
