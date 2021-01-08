

package cn.ds.transaction.transfer.saga;

import static cn.ds.transaction.framework.enums.EventType.SagaStartedEvent;

import java.util.concurrent.BlockingQueue;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import cn.ds.transaction.framework.SagaSvrResponse;
import cn.ds.transaction.framework.interfaces.MessageSender;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.TxEvent;

public class RetryableMessageSender implements SagaMessageSender {
  private final BlockingQueue<MessageSender> availableMessageSenders;

  public RetryableMessageSender(BlockingQueue<MessageSender> availableMessageSenders) {
    this.availableMessageSenders = availableMessageSenders;
  }

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
    if (event.type() == SagaStartedEvent) {
      throw new SagaException("Failed to process subsequent requests because no saga server is available");
    }
    try {
      return ((SagaMessageSender)availableMessageSenders.take()).send(event);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SagaException("Failed to send event " + event + " due to interruption", e);
    }
  }
}
