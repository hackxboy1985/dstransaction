

package cn.ds.transaction.framework;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import cn.ds.transaction.framework.context.SagaServerMetas;
import cn.ds.transaction.framework.context.IdGenerator;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.context.UniqueIdGenerator;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.messageHandlerImpl.CompensationMessageHandler;
import org.junit.Before;
import org.junit.Test;

public class CompensationMessageHandlerTest {

  private final List<TxEvent> events = new ArrayList<>();
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
      return "UNKNOWN"; }

    @Override
    public SagaSvrResponse send(TxEvent event) {
      events.add(event);
      return new SagaSvrResponse(false);
    }
  };

  private final String globalTxId = uniquify("globalTxId");
  private final String localTxId = uniquify("localTxId");
  private final String parentTxId = uniquify("parentTxId");

  private final String compensationMethod = getClass().getCanonicalName();
  private final String payload = uniquify("blah");

  @Before
  public void setUp() {
    events.clear();
  }

  @Test
  public void sendsCompensatedEventOnCompensationCompleted() {
    final CallbackContext context = mock(CallbackContext.class);
    final CompensationMessageHandler handler = new CompensationMessageHandler(sender, context);
    IdGenerator<String> idGenerator = new UniqueIdGenerator();
    SagaContext sagaContext = new SagaContext(idGenerator, SagaServerMetas.builder().akkaEnabled(false).build());
    when(context.getSagaContext()).thenReturn(sagaContext);
    handler.onReceive(globalTxId, localTxId, parentTxId, compensationMethod, payload);
    assertThat(events.size(), is(1));
    TxEvent event = events.get(0);
    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.type(), is(EventType.TxCompensatedEvent));
    assertThat(event.compensationMethod(), is(getClass().getCanonicalName()));
    assertThat(event.payloads().length, is(0));
    verify(context).apply(globalTxId, localTxId, parentTxId, compensationMethod, payload);
  }

  @Test
  public void sendsCompensateAckSucceedEventOnCompensationCompletedWithFSM() throws NoSuchMethodException {
    IdGenerator<String> idGenerator = new UniqueIdGenerator();
    SagaContext sagaContext = new SagaContext(idGenerator, SagaServerMetas.builder().akkaEnabled(true).build());
    CallbackContext context = new CallbackContext(sagaContext, sender);
    Method mockMethod = this.getClass().getMethod("mockCompensationSucceedMethod",String.class);
    context.addCallbackContext(compensationMethod, mockMethod, this);
    CompensationMessageHandler handler = new CompensationMessageHandler(sender, context);
    handler.onReceive(globalTxId, localTxId, parentTxId, compensationMethod, payload);
    assertThat(events.size(), is(1));
    TxEvent event = events.get(0);
    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.type(), is(EventType.TxCompensateAckSucceedEvent));
    assertThat(event.compensationMethod(), is(getClass().getCanonicalName()));
    assertThat(event.payloads().length, is(0));
  }

  @Test
  public void sendsCompensateAckFailedEventOnCompensationFailedWithFSM() throws NoSuchMethodException {
    IdGenerator<String> idGenerator = new UniqueIdGenerator();
    SagaContext sagaContext = new SagaContext(idGenerator, SagaServerMetas.builder().akkaEnabled(true).build());
    CallbackContext context = new CallbackContext(sagaContext, sender);
    Method mockMethod = this.getClass().getMethod("mockCompensationFailedMethod",String.class);
    context.addCallbackContext(compensationMethod, mockMethod, this);
    CompensationMessageHandler handler = new CompensationMessageHandler(sender, context);
    handler.onReceive(globalTxId, localTxId, parentTxId, compensationMethod, payload);
    assertThat(events.size(), is(1));
    TxEvent event = events.get(0);
    assertThat(event.globalTxId(), is(globalTxId));
    assertThat(event.localTxId(), is(localTxId));
    assertThat(event.parentTxId(), is(parentTxId));
    assertThat(event.type(), is(EventType.TxCompensateAckFailedEvent));
    assertThat(event.compensationMethod(), is(getClass().getCanonicalName()));
    assertThat(event.payloads().length, greaterThan(0));
  }

  public void mockCompensationSucceedMethod(String payloads){
    // mock compensation method
  }

  public void mockCompensationFailedMethod(String payloads){
    // mock compensation method
    throw new RuntimeException("mock compensation failed");
  }
}
