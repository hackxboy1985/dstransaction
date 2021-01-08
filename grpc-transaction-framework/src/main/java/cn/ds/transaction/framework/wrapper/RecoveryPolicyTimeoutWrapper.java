
package cn.ds.transaction.framework.wrapper;

import java.nio.channels.ClosedByInterruptException;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.recovery.AbstractRecoveryPolicy;
import cn.ds.transaction.framework.interceptor.CompensableInterceptor;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.exception.TransactionTimeoutException;
import cn.ds.transaction.framework.annotations.Compensable;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 恢复策略的装饰类-增加超时检测
 * RecoveryPolicy Wrapper
 * 1.Use this wrapper to send a request if the @compensable forwardTimeout greaterThan 0
 * 2.Terminate thread execution if execution time is greater than the forwardTimeout of @compensable
 *
 * Exception
 * 1.If the interrupt succeeds, a TransactionTimeoutException is thrown and the local transaction is rollback
 * 2.If the interrupt fails, throw an SagaException
 *
 * Note: Saga end thread coding advice
 * 1.add short sleep to while true loop. Otherwise, the thread may not be able to terminate.
 * 2.Replace the synchronized with ReentrantLock, Otherwise, the thread may not be able to terminate.
 * */

public class RecoveryPolicyTimeoutWrapper {

  private AbstractRecoveryPolicy recoveryPolicy;

  public RecoveryPolicyTimeoutWrapper(AbstractRecoveryPolicy recoveryPolicy) {
    this.recoveryPolicy = recoveryPolicy;
  }

  public Object applyTo(ProceedingJoinPoint joinPoint, Compensable compensable,
                        CompensableInterceptor interceptor, SagaContext context, String parentTxId, int retries)
      throws Throwable {
    final TimeoutProb timeoutProb = TimeoutProbManager.getInstance().addTimeoutProb(compensable.forwardTimeout());
    Object output;
    try {
      output = this.recoveryPolicy
          .applyTo(joinPoint, compensable, interceptor, context, parentTxId, retries);
      if (timeoutProb.getInterruptFailureException() != null) {
        throw new SagaException(timeoutProb.getInterruptFailureException());
      }
    } catch (InterruptedException e) {
      if (timeoutProb.getInterruptFailureException() != null) {
        throw new SagaException(timeoutProb.getInterruptFailureException());
      }else{
        throw new TransactionTimeoutException(e.getMessage(),e);
      }
    } catch (IllegalMonitorStateException e) {
      if (timeoutProb.getInterruptFailureException() != null) {
        throw new SagaException(timeoutProb.getInterruptFailureException());
      }else{
        throw new TransactionTimeoutException(e.getMessage(),e);
      }
    } catch (ClosedByInterruptException e) {
      if (timeoutProb.getInterruptFailureException() != null) {
        throw new SagaException(timeoutProb.getInterruptFailureException());
      }else{
        throw new TransactionTimeoutException(e.getMessage(),e);
      }
    } catch (Throwable e) {
      throw e;
    } finally {
      TimeoutProbManager.getInstance().removeTimeoutProb(timeoutProb);
    }
    return output;
  }
}
