
package cn.ds.transaction.framework.interceptor;

import cn.ds.transaction.framework.AlphaResponse;

public class NoOpEventAwareInterceptor implements EventAwareInterceptor {

  public static final NoOpEventAwareInterceptor INSTANCE = new NoOpEventAwareInterceptor();

  @Override
  public AlphaResponse preIntercept(String parentTxId, String compensationMethod, int timeout,
                                    String retriesMethod, int forwardRetries, int forwardTimeout, int reverseRetries,
                                    int reverseTimeout, int retryDelayInMilliseconds, Object... message) {
    return new AlphaResponse(false);
  }

  @Override
  public void postIntercept(String parentTxId, String compensationMethod) {
    // NoOp
  }

  @Override
  public void onError(String parentTxId, String compensationMethod, Throwable throwable) {
    // NoOp
  }
}
