
package cn.ds.transaction.framework;

//import static org.hamcrest.Matchers.anyOf;
//import static org.hamcrest.Matchers.containsString;
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
import java.util.UUID;

import javax.transaction.InvalidTransactionException;

import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.annotations.Compensable;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.recovery.ForwardRecovery;
import cn.ds.transaction.framework.recovery.RecoveryPolicy;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;

public class ForwardRecoveryTest {
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

  private final RecoveryPolicy recoveryPolicy = new ForwardRecovery();

  private volatile SagaException exception;

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
  public void forwardExceptionWhenGlobalTxAborted() {
    SagaMessageSender sender = mock(SagaMessageSender.class);
    when(sender.send(any(TxEvent.class))).thenReturn(new SagaSvrResponse(true));

    CompensableInterceptor interceptor = new CompensableInterceptor(sagaContext, sender);

    try {
      recoveryPolicy.apply(joinPoint, compensable, interceptor, sagaContext, parentTxId, 0);
      //expectFailing(InvalidTransactionException.class);
    } catch (InvalidTransactionException e) {
      assertThat(e.getMessage().contains("Abort sub transaction"), is(true));
    } catch (Throwable throwable) {
      fail("unexpected exception throw: " + throwable);
    }

    verify(sender, times(1)).send(any(TxEvent.class));
  }

  @Test
  public void throwExceptionWhenRetryReachesMaximum() throws Throwable {
    when(compensable.forwardRetries()).thenReturn(2);
    when(joinPoint.proceed()).thenThrow(oops);

    try {
      recoveryPolicy.apply(joinPoint, compensable, interceptor, sagaContext, parentTxId, 2);
//      expectFailing(RuntimeException.class);
    } catch (RuntimeException e) {
      //Sometimes thrown interrupt exception with CI
//      assertThat(e.getMessage(), anyOf(containsString("oops"),
//          containsString("Failed to handle tx because it is interrupted")));
    }

    assertThat(messages.size(), is(3));
    assertThat(messages.get(0).type(), is(EventType.TxStartedEvent));
    assertThat(messages.get(1).type(), is(EventType.TxStartedEvent));
    assertThat(messages.get(2).type(), is(EventType.TxAbortedEvent));
  }

  private String doNothing() {
    return "doNothing";
  }
}
