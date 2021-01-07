

package cn.ds.transaction.transfer.core;

import io.grpc.stub.StreamObserver;
import java.lang.invoke.MethodHandles;

import cn.ds.transaction.framework.interfaces.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ReconnectStreamObserver<T> implements StreamObserver<T> {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final LoadBalanceContext loadContext;

  private final MessageSender messageSender;

  public ReconnectStreamObserver(
      LoadBalanceContext loadContext, MessageSender messageSender) {
    this.loadContext = loadContext;
    this.messageSender = messageSender;
  }

  @Override
  public void onError(Throwable t) {
    LOG.error("Failed to process grpc coordinate command.", t);
    loadContext.getGrpcOnErrorHandler().handle(messageSender);
  }

  @Override
  public void onCompleted() {
    // Do nothing here
  }
}
