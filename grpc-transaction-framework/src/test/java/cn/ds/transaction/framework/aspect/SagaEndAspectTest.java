

package cn.ds.transaction.framework.aspect;

import cn.ds.transaction.framework.AlphaResponse;
import cn.ds.transaction.framework.TxEvent;
import cn.ds.transaction.framework.annotations.SagaEnd;
import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.OmegaContext;
import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SagaEndAspectTest {
  private final List<TxEvent> messages = new ArrayList<>();
  private final String globalTxId = UUID.randomUUID().toString();

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
  private final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
  private final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

  @SuppressWarnings("unchecked")
  private final IdGenerator<String> idGenerator = Mockito.mock(IdGenerator.class);
  private final SagaEnd sagaEnd = Mockito.mock(SagaEnd.class);

  private final OmegaContext omegaContext = Mockito.mock(OmegaContext.class);
  private SagaEndAspect aspect;

  @Before
  public void setUp() throws Exception {
    when(omegaContext.globalTxId()).thenReturn(globalTxId);
    when(omegaContext.localTxId()).thenReturn(globalTxId);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("doNothing"));
  }

  @Test
  public void sagaEndWithoutError() throws Throwable {
    aspect = new SagaEndAspect(sender, omegaContext);
    aspect.advise(joinPoint, sagaEnd);
    assertThat(messages.size(), is(1));
    TxEvent endedEvent = messages.get(0);

    assertThat(endedEvent.globalTxId(), is(globalTxId));
    assertThat(endedEvent.localTxId(), is(globalTxId));
    assertThat(endedEvent.parentTxId(), is(nullValue()));
    assertThat(endedEvent.type(), is(EventType.SagaEndedEvent));

    verify(omegaContext).clear();
  }



  @Test
  public void sagaEndWithErrors() throws Throwable {

    aspect = new SagaEndAspect(sender, omegaContext);
    RuntimeException oops = new RuntimeException("oops");

    when(joinPoint.proceed()).thenThrow(oops);

    try {
      aspect.advise(joinPoint, sagaEnd);
      expectFailing(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e, is(oops));
    }

    assertThat(messages.size(), is(1));
    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.type(), is(EventType.SagaAbortedEvent));

    verify(omegaContext).clear();
  }




  private String doNothing() {
    return "doNothing";
  }
}
