

package cn.ds.transaction.framework.aspect;

//import static com.seanyinx.github.unit.scaffolding.AssertUtils.expectFailing;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.ds.transaction.framework.annotations.SagaStart;
import cn.ds.transaction.framework.context.SagaServerMetas;
import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.SagaSvrResponse;
import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.TxEvent;
import cn.ds.transaction.framework.processor.SagaStartAnnotationProcessor;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito.mockStatic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SagaStartAspectTest {
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
    public SagaSvrResponse send(TxEvent event) {
      messages.add(event);
      return new SagaSvrResponse(false);
    }
  };
  private final ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
  private final MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

  @SuppressWarnings("unchecked")
  private final IdGenerator<String> idGenerator = Mockito.mock(IdGenerator.class);
  private final SagaStart sagaStart = Mockito.mock(SagaStart.class);
  private static final Logger loggerMock = Mockito.mock(Logger.class);
  private static final LoggerFactory loggerFactory = Mockito.mock(LoggerFactory.class);

  private SagaContext sagaContext;
  private SagaStartAspect aspect;

  @Before
  public void setUp() throws Exception {
    when(idGenerator.nextId()).thenReturn(globalTxId);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethod("doNothing"));
    // setup the default value of SagaStart
    when(sagaStart.autoClose()).thenReturn(true);

//    Mockito.mock()
//    mockStatic(LoggerFactory.class);

    when(LoggerFactory.getLogger(SagaStartAnnotationProcessor.class)).thenReturn(loggerMock);
  }

  @Test
  public void newGlobalTxIdInSagaStart() throws Throwable {
    sagaContext = new SagaContext(idGenerator);
    aspect = new SagaStartAspect(sender, sagaContext);
    aspect.advise(joinPoint, sagaStart);

    TxEvent startedEvent = messages.get(0);

    assertThat(startedEvent.globalTxId(), is(globalTxId));
    assertThat(startedEvent.localTxId(), is(globalTxId));
    assertThat(startedEvent.parentTxId(), is(nullValue()));
    assertThat(startedEvent.type(), is(EventType.SagaStartedEvent));

    TxEvent endedEvent = messages.get(1);

    assertThat(endedEvent.globalTxId(), is(globalTxId));
    assertThat(endedEvent.localTxId(), is(globalTxId));
    assertThat(endedEvent.parentTxId(), is(nullValue()));
    assertThat(endedEvent.type(), is(EventType.SagaEndedEvent));

    assertThat(sagaContext.globalTxId(), is(nullValue()));
    assertThat(sagaContext.localTxId(), is(nullValue()));
  }

  @Test
  public void dontSendingSagaEndMessage() throws Throwable {
    when(sagaStart.autoClose()).thenReturn(false);
    sagaContext = new SagaContext(idGenerator);
    aspect = new SagaStartAspect(sender, sagaContext);

    aspect.advise(joinPoint, sagaStart);
    assertThat(messages.size(), is(1));
    TxEvent startedEvent = messages.get(0);

    assertThat(startedEvent.globalTxId(), is(globalTxId));
    assertThat(startedEvent.localTxId(), is(globalTxId));
    assertThat(startedEvent.parentTxId(), is(nullValue()));
    assertThat(startedEvent.type(), is(EventType.SagaStartedEvent));

    assertThat(sagaContext.globalTxId(), is(nullValue()));
    assertThat(sagaContext.localTxId(), is(nullValue()));
  }

  @Test
  public void clearContextOnSagaStartError() throws Throwable {
    sagaContext = new SagaContext(idGenerator);
    aspect = new SagaStartAspect(sender, sagaContext);
    RuntimeException oops = new RuntimeException("oops");

    when(joinPoint.proceed()).thenThrow(oops);

    try {
      aspect.advise(joinPoint, sagaStart);
//      expectFailing(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e, is(oops));
    }

    assertThat(messages.size(), is(2));
    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.type(), is(EventType.SagaStartedEvent));

    event = messages.get(1);
    
    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.type(), is(EventType.TxAbortedEvent));

    assertThat(sagaContext.globalTxId(), is(nullValue()));
    assertThat(sagaContext.localTxId(), is(nullValue()));
  }

  @Test
  public void clearContextOnSagaStartErrorWithAkka() throws Throwable {
    sagaContext = new SagaContext(idGenerator, SagaServerMetas.builder().akkaEnabled(true).build());
    aspect = new SagaStartAspect(sender, sagaContext);
    RuntimeException oops = new RuntimeException("oops");

    when(joinPoint.proceed()).thenThrow(oops);

    try {
      aspect.advise(joinPoint, sagaStart);
//      expectFailing(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e, is(oops));
    }

    assertThat(messages.size(), is(2));
    TxEvent event = messages.get(0);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.type(), is(EventType.SagaStartedEvent));

    event = messages.get(1);

    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(globalTxId));
    assertThat(event.parentTxId(), is(nullValue()));
    assertThat(event.type(), is(EventType.SagaAbortedEvent));

    assertThat(sagaContext.globalTxId(), is(nullValue()));
    assertThat(sagaContext.localTxId(), is(nullValue()));
  }

  private String doNothing() {
    return "doNothing";
  }
}
