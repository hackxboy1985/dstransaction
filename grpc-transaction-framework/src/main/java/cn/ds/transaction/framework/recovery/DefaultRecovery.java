
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
 * DefaultRecovery is used to execute business logic once.
 * The corresponding events will report to Saga server before and after the execution of business logic.
 * If there are errors while executing the business logic, a TxAbortedEvent will be reported to SagaSvrver.
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
    LOG.debug("Intercepting compensable method {} with context {}", method.toString(), context);

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
      interceptor.postIntercept(parentTxId, compensationSignature);

      return result;
    } catch (Throwable throwable) {
      if (compensable.forwardRetries() == 0 || (compensable.forwardRetries() > 0
          && forwardRetries == 1)) {
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
