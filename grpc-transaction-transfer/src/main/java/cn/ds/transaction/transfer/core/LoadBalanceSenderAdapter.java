

package cn.ds.transaction.transfer.core;

import cn.ds.transaction.framework.TxEvent;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.grpc.protocol.ServerMeta;
import com.google.common.base.Optional;
import io.grpc.ManagedChannel;
import java.lang.invoke.MethodHandles;
import cn.ds.transaction.framework.AlphaResponse;
import cn.ds.transaction.framework.interfaces.MessageSender;
import cn.ds.transaction.framework.exception.OmegaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoadBalanceSenderAdapter implements MessageSender {

  private final LoadBalanceContext loadContext;

  private final MessageSenderPicker senderPicker;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public LoadBalanceSenderAdapter(
      LoadBalanceContext loadContext,
      MessageSenderPicker senderPicker) {
    this.loadContext = loadContext;
    this.senderPicker = senderPicker;
  }

  @SuppressWarnings("unchecked")
  public <T> T pickMessageSender() {
    return (T) senderPicker.pick(loadContext.getSenders(),
        loadContext.getGrpcOnErrorHandler().getGrpcRetryContext().getDefaultMessageSender());
  }

  public <T> Optional<AlphaResponse> doGrpcSend(MessageSender messageSender, T event, SenderExecutor<T> executor) {
    AlphaResponse response = null;
    try {
      long startTime = System.nanoTime();
      response = executor.apply(event);
      loadContext.getSenders().put(messageSender, System.nanoTime() - startTime);
    } catch (OmegaException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Saga-Transaction::Retry sending event {} due to failure", event, e);
      loadContext.getSenders().put(messageSender, Long.MAX_VALUE);
    }
    return Optional.fromNullable(response);
  }

  public Optional<AlphaResponse> doGrpcSend2(TxEvent event) {
    final SagaMessageSender messageSender = pickMessageSender();

    AlphaResponse response = null;
    try {
      long startTime = System.nanoTime();
      response =  messageSender.send(event);
      loadContext.getSenders().put(messageSender, System.nanoTime() - startTime);
    } catch (OmegaException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Saga-Transaction::Retry sending event {} due to failure", event, e);
      loadContext.getSenders().put(messageSender, Long.MAX_VALUE);
    }
    return Optional.fromNullable(response);
  }

  @Override
  public void onConnected() {
    for(MessageSender sender : loadContext.getSenders().keySet()){
      try {
        sender.onConnected();
      } catch (Exception e) {
        LOG.error("Saga-Transaction::Failed connecting to alpha at {}", sender.target(), e);
      }
    }
  }

  @Override
  public void onDisconnected() {
    for (MessageSender sender : loadContext.getSenders().keySet()) {
      try {
        sender.onDisconnected();
      } catch (Exception e) {
        LOG.error("Saga-Transaction::Failed disconnecting from alpha at {}", sender.target(), e);
      }
    }
  }

  @Override
  public ServerMeta onGetServerMeta() {
    boolean metaConsistency = true;
    ServerMeta serverMeta = null;
    for (MessageSender sender : loadContext.getSenders().keySet()) {
      try {
        if (serverMeta == null) {
          serverMeta = sender.onGetServerMeta();
          LOG.info("Saga-Transaction::Alpha configuration is " + serverMeta.getMetaMap());
        } else {
          ServerMeta otherServerMeta = sender.onGetServerMeta();
          if (!serverMeta.getMetaMap().equals(otherServerMeta.getMetaMap())) {
            metaConsistency = false;
            LOG.warn("Saga-Transaction::Alpha configuration is " + otherServerMeta.getMetaMap());
          }
        }
        if (!metaConsistency) {
          throw new Exception("Using different Alpha configuration with multiple Alpha");
        }
      } catch (Exception e) {
        LOG.error("Saga-Transaction::Failed disconnecting from alpha at {}", sender.target(), e);
      }
    }
    return serverMeta;
  }

  @Override
  public void close() {
    loadContext.getPendingTaskRunner().shutdown();
    for(ManagedChannel channel : loadContext.getChannels()) {
      channel.shutdownNow();
    }
  }

  @Override
  public String target() {
    return "UNKNOWN";
  }

  public MessageSenderPicker getSenderPicker() {
    return senderPicker;
  }

  public LoadBalanceContext getLoadContext() {
    return loadContext;
  }
}
