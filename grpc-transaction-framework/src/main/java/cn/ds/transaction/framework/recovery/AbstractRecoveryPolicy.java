
package cn.ds.transaction.framework.recovery;

import cn.ds.transaction.framework.annotations.Compensable;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.wrapper.RecoveryPolicyTimeoutWrapper;
import cn.ds.transaction.framework.context.SagaContext;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class AbstractRecoveryPolicy implements RecoveryPolicy {

  public abstract Object applyTo(ProceedingJoinPoint joinPoint, Compensable compensable,
                                 CompensableInterceptor interceptor, SagaContext context, String parentTxId, int forwardRetries)
      throws Throwable;

  @Override
  public Object apply(ProceedingJoinPoint joinPoint, Compensable compensable,
                      CompensableInterceptor interceptor, SagaContext context, String parentTxId, int forwardRetries)
      throws Throwable {
    Object result;
    if(compensable.forwardTimeout()>0){
      RecoveryPolicyTimeoutWrapper wrapper = new RecoveryPolicyTimeoutWrapper(this);
      result = wrapper.applyTo(joinPoint, compensable, interceptor, context, parentTxId, forwardRetries);
    } else {
      result = this.applyTo(joinPoint, compensable, interceptor, context, parentTxId, forwardRetries);
    }
    return result;
  }


}
