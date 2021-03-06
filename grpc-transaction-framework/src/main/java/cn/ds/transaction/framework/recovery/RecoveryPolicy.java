
package cn.ds.transaction.framework.recovery;

import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.annotations.Compensable;
import org.aspectj.lang.ProceedingJoinPoint;

public interface RecoveryPolicy {
  Object apply(ProceedingJoinPoint joinPoint, Compensable compensable, CompensableInterceptor interceptor,
               SagaContext context, String parentTxId, int forwardRetries) throws Throwable;
}
