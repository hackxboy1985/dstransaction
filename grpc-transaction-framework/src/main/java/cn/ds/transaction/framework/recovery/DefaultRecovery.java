
package cn.ds.transaction.framework.recovery;

import cn.ds.transaction.framework.SagaSvrResponse;
import cn.ds.transaction.framework.annotations.Compensable;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.InvalidTransactionException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * 缺省补偿即回滚,相应的事件将在执行业务逻辑之前和之后报告给Saga服务器
 * 如果在执行业务逻辑时出现错误，则会向报告TxAbortedEvent给SagaServer
 *
 *                 pre                       post
 *     request --------- 2.business logic --------- response
 *                 \                          |
 * 1.TxStartedEvent \                        | 3.TxEndedEvent
 *                   \                      |
 *                    ----------------------
 *                            TxServer
 */
public class DefaultRecovery extends AbstractRecoveryPolicy {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Object applyTo(ProceedingJoinPoint joinPoint, Compensable compensable, CompensableInterceptor interceptor,
                        SagaContext context, String parentTxId, int forwardRetries) throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    LOG.debug("Saga-Transaction::DefaultRecovery compensable method {} with context {}", method.toString(), context);

    String compensationSignature =
        compensable.compensationMethod().isEmpty() ? "" : compensationMethodSignature(joinPoint, compensable, method);

    String retrySignature = (forwardRetries != 0 || compensationSignature.isEmpty()) ? method.toString() : "";

    //TODO:send TxStartedEvent
    SagaSvrResponse response = interceptor.preIntercept(parentTxId, compensationSignature, compensable.forwardTimeout(),
            retrySignature, forwardRetries, compensable.forwardTimeout(),
            compensable.reverseRetries(), compensable.reverseTimeout(), compensable.retryDelayInMilliseconds(), joinPoint.getArgs());
    if (response.aborted()) {
      String abortedLocalTxId = context.localTxId();
      context.setLocalTxId(parentTxId);
      throw new InvalidTransactionException("Abort sub transaction " + abortedLocalTxId +
          " because global transaction " + context.globalTxId() + " has already aborted.");
    }

    try {
      Object result = joinPoint.proceed();

      //TODO:send TxEndedEvent
      interceptor.postIntercept(parentTxId, compensationSignature);
      return result;
    } catch (Throwable throwable) {
      if (compensable.forwardRetries() == 0 || (compensable.forwardRetries() > 0
          && forwardRetries == 1)) {
        //TODO:send TxAbortedEvent
        interceptor.onError(parentTxId, compensationSignature, throwable);
      }
      throw throwable;
    }
  }

  String compensationMethodSignature(ProceedingJoinPoint joinPoint, Compensable compensable, Method method)
      throws NoSuchMethodException {
    return joinPoint.getTarget()
        .getClass()
        .getDeclaredMethod(compensable.compensationMethod(), method.getParameterTypes())
        .toString();
  }
}
