

package cn.ds.transaction.framework.interceptor;

import cn.ds.transaction.framework.AlphaResponse;

public interface EventAwareInterceptor {

  AlphaResponse preIntercept(String parentTxId, String compensationMethod, int timeout,
                             String retriesMethod, int forwardRetries, int forwardTimeout, int reverseRetries,
                             int reverseTimeout, int retryDelayInMilliseconds,
                             Object... message);

  void postIntercept(String parentTxId, String compensationMethod);

  void onError(String parentTxId, String compensationMethod, Throwable throwable);
}
