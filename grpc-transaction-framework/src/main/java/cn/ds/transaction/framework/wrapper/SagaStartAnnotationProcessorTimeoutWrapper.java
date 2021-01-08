

package cn.ds.transaction.framework.wrapper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.channels.ClosedByInterruptException;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.annotations.SagaStart;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.processor.SagaStartAnnotationProcessor;
import cn.ds.transaction.framework.exception.TransactionTimeoutException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SagaStartAnnotationProcessorTimeoutWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final SagaStartAnnotationProcessor sagaStartAnnotationProcessor;

  public SagaStartAnnotationProcessorTimeoutWrapper(
      SagaStartAnnotationProcessor sagaStartAnnotationProcessor) {
    this.sagaStartAnnotationProcessor = sagaStartAnnotationProcessor;
  }

  public Object apply(ProceedingJoinPoint joinPoint, SagaStart sagaStart, SagaContext context)
      throws Throwable {
    final TimeoutProb timeoutProb = TimeoutProbManager.getInstance()
        .addTimeoutProb(sagaStart.timeout());
    Object output;
    try {
      Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
      sagaStartAnnotationProcessor.preIntercept(sagaStart.timeout());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Initialized context {} before execution of method {}", context,
            method.toString());
      }
      try {
        output = joinPoint.proceed();
        if (timeoutProb.getInterruptFailureException() != null) {
          throw new SagaException(timeoutProb.getInterruptFailureException());
        }
        if (sagaStart.autoClose()) {
          sagaStartAnnotationProcessor.postIntercept(context.globalTxId());
          if (LOG.isDebugEnabled()) {
            LOG.debug("Transaction with context {} has finished.", context);
          }
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Transaction with context {} is not finished in the SagaStarted annotated method.", context);
          }
        }
        return output;
      } catch (Throwable throwable) {
        // TODO We still need to intercept some exceptions that we can't judge the state of the child transaction.
        //  At this point, we don't need to send SagaAbortEvent, just need to throw a TransactionTimeoutException
        //  For example, java.net.SocketTimeoutException, etc.
        if (LOG.isDebugEnabled()) {
          LOG.debug("TimeoutWrapper exception {}", throwable.getClass().getName());
        }
        if (timeoutProb.getInterruptFailureException() != null) {
          LOG.info("TimeoutProb interrupt fail");
          throw timeoutProb.getInterruptFailureException();
        } else if (isThreadInterruptException(throwable)) {
          // We don't have to send an SagaAbortEvent
          // Because the SagaActor state automatically change to suspended when timeout.
          throw new TransactionTimeoutException("Timeout interrupt", throwable);
        } else {
          // We don't need to handle the SagaException here
          if (!(throwable instanceof SagaException)) {
            LOG.info("TimeoutWrapper Exception {}", throwable.getClass().getName());
            sagaStartAnnotationProcessor.onError(method.toString(), throwable);
            LOG.error("Transaction {} failed.", context.globalTxId());
          }
        }
        throw throwable;
      }
    } finally {
      context.clear();
      TimeoutProbManager.getInstance().removeTimeoutProb(timeoutProb);
    }
  }

  private boolean isThreadInterruptException(Throwable throwable) {
    if (throwable instanceof InterruptedException ||
        throwable instanceof IllegalMonitorStateException ||
        throwable instanceof ClosedByInterruptException ||
        throwable.getCause() instanceof InterruptedException ||
        throwable.getCause() instanceof IllegalMonitorStateException ||
        throwable.getCause() instanceof ClosedByInterruptException) {
      return true;
    } else {
      return false;
    }
  }
}
