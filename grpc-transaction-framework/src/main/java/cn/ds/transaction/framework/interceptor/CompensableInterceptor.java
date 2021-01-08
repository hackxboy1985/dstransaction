

package cn.ds.transaction.framework.interceptor;

import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.*;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;

public class CompensableInterceptor implements EventAwareInterceptor {
  private final SagaContext context;
  private final SagaMessageSender sender;

  public CompensableInterceptor(SagaContext context, SagaMessageSender sender) {
    this.sender = sender;
    this.context = context;
  }

  @Override
  public SagaSvrResponse preIntercept(String parentTxId, String compensationMethod, int timeout, String retriesMethod,
                                      int forwardRetries, int forwardTimeout, int reverseRetries, int reverseTimeout, int retryDelayInMilliseconds, Object... message) {
    return sender.send(new TxStartedEvent(context.globalTxId(), context.localTxId(), parentTxId, compensationMethod,
        timeout, retriesMethod, forwardRetries, forwardTimeout, reverseRetries, reverseTimeout, retryDelayInMilliseconds, message));
  }

  @Override
  public void postIntercept(String parentTxId, String compensationMethod) {
    sender.send(new TxEndedEvent(context.globalTxId(), context.localTxId(), parentTxId, compensationMethod));
  }

  @Override
  public void onError(String parentTxId, String compensationMethod, Throwable throwable) {
    sender.send(
        new TxAbortedEvent(context.globalTxId(), context.localTxId(), parentTxId, compensationMethod, throwable));
  }

}
