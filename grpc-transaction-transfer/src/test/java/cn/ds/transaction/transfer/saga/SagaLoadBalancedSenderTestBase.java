

package cn.ds.transaction.transfer.saga;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;

import cn.ds.transaction.framework.enums.EventType;
import cn.ds.transaction.framework.interfaces.MessageDeserializer;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.MessageSerializer;
import cn.ds.transaction.framework.TxEvent;
import cn.ds.transaction.grpc.protocol.GrpcAck;
import cn.ds.transaction.grpc.protocol.GrpcCompensateCommand;
import cn.ds.transaction.grpc.protocol.GrpcServiceConfig;
import cn.ds.transaction.grpc.protocol.GrpcTxEvent;
import cn.ds.transaction.grpc.protocol.TxEventServiceGrpc.TxEventServiceImplBase;
import org.junit.After;
import org.junit.AfterClass;

import io.grpc.Server;
import io.grpc.stub.StreamObserver;

public abstract class SagaLoadBalancedSenderTestBase {
  protected static final int[] ports = {8080, 8090};

  protected static final Map<Integer, Server> servers = new HashMap<>();

  protected static final Map<Integer, Integer> delays = new HashMap<Integer, Integer>() {{
    put(8080, 0);
    put(8090, 800);
  }};

  protected static final Map<Integer, Queue<String>> connected = new HashMap<Integer, Queue<String>>() {{
    put(8080, new ConcurrentLinkedQueue<String>());
    put(8090, new ConcurrentLinkedQueue<String>());
  }};

  protected static final Map<Integer, Queue<TxEvent>> eventsMap = new HashMap<Integer, Queue<TxEvent>>() {{
    put(8080, new ConcurrentLinkedQueue<TxEvent>());
    put(8090, new ConcurrentLinkedQueue<TxEvent>());
  }};

  protected final List<String> compensated = new ArrayList<>();

  protected final String globalTxId = uniquify("globalTxId");

  protected final String localTxId = uniquify("localTxId");

  protected final String parentTxId = uniquify("parentTxId");

  protected final String compensationMethod = getClass().getCanonicalName();

  protected final TxEvent event = new TxEvent(EventType.TxStartedEvent, globalTxId, localTxId, parentTxId,
      compensationMethod, 0, "", 0, 0, 0, 0, 0, "blah");

  protected final String serviceName = uniquify("serviceName");

  protected final MessageSerializer serializer = new MessageSerializer() {
    @Override
    public byte[] serialize(Object[] objects) {
      return objects[0].toString().getBytes();
    }
  };

  protected final MessageDeserializer deserializer = new MessageDeserializer() {

    @Override
    public Object[] deserialize(byte[] message) {
      return new Object[] {new String(message)};
    }
  };

  protected final MessageHandler handler = new MessageHandler() {

    @Override
    public void onReceive(String globalTxId, String localTxId, String parentTxId, String compensationMethod,
        Object... payloads) {
      compensated.add(globalTxId);

    }
  };

  protected final String[] addresses = {"localhost:8080", "localhost:8090"};

  protected final SagaLoadBalanceSender messageSender = newMessageSender(addresses);

  @AfterClass
  public static void tearDown() {
    for(Server server: servers.values()) {
      server.shutdown();
    }
  }

  protected abstract SagaLoadBalanceSender newMessageSender(String[] addresses);

  @After
  public void after() {
    messageSender.onDisconnected();
    messageSender.close();
    for (Queue<TxEvent> queue :eventsMap.values()) {
      queue.clear();
    }
    for (Queue<String> queue :connected.values()) {
      queue.clear();
    }
  }

  protected static class MyTxEventService extends TxEventServiceImplBase {
    private final Queue<String> connected;
    private final Queue<TxEvent> events;
    private final int delay;

    private StreamObserver<GrpcCompensateCommand> responseObserver;

    protected MyTxEventService(Queue<String> connected, Queue<TxEvent> events, int delay) {
      this.connected = connected;
      this.events = events;
      this.delay = delay;
    }

    @Override
    public StreamObserver<GrpcServiceConfig> onConnected(final StreamObserver<GrpcCompensateCommand> responseObserver) {
      this.responseObserver = responseObserver;
      return new StreamObserver<GrpcServiceConfig>() {

        @Override
        public void onNext(GrpcServiceConfig grpcServiceConfig) {
          connected.add("Connected " + grpcServiceConfig.getServiceName());
        }

        @Override
        public void onError(Throwable throwable) {
          throw new RuntimeException(throwable);
        }

        @Override
        public void onCompleted() {
          // Do nothing here
        }
      };
    }

    @Override
    public void onTxEvent(GrpcTxEvent request, StreamObserver<GrpcAck> responseObserver) {
      events.offer(new TxEvent(
          EventType.valueOf(request.getType()),
          request.getGlobalTxId(),
          request.getLocalTxId(),
          request.getParentTxId(),
          request.getCompensationMethod(),
          request.getForwardTimeout(),
          request.getRetryMethod(),
          request.getForwardRetries(),
          request.getForwardTimeout(),
          request.getReverseRetries(),
          request.getReverseTimeout(),
          request.getRetryDelayInMilliseconds(),
          new String(request.getPayloads().toByteArray())));

      sleep();

      if (EventType.TxAbortedEvent.name().equals(request.getType())) {
        this.responseObserver.onNext(GrpcCompensateCommand
            .newBuilder()
            .setGlobalTxId(request.getGlobalTxId())
            .build());
      }

      if ("TxStartedEvent".equals(request.getType()) && request.getCompensationMethod().equals("reject")) {
        responseObserver.onNext(GrpcAck.newBuilder().setAborted(true).build());
      } else {
        responseObserver.onNext(GrpcAck.newBuilder().setAborted(false).build());
      }

      responseObserver.onCompleted();
    }

    private void sleep() {
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        fail(e.getMessage());
      }
    }

    @Override
    public void onDisconnected(GrpcServiceConfig request, StreamObserver<GrpcAck> responseObserver) {
      connected.add("Disconnected " + request.getServiceName());
      responseObserver.onNext(GrpcAck.newBuilder().build());
      responseObserver.onCompleted();
    }
  }
}
