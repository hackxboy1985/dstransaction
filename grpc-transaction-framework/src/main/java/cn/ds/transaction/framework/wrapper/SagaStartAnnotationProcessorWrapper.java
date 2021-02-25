

package cn.ds.transaction.framework.wrapper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.annotations.SagaStart;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.processor.SagaStartAnnotationProcessor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SagaStartAnnotationProcessorWrapper {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final SagaStartAnnotationProcessor sagaStartAnnotationProcessor;

  public SagaStartAnnotationProcessorWrapper(
      SagaStartAnnotationProcessor sagaStartAnnotationProcessor) {
    this.sagaStartAnnotationProcessor = sagaStartAnnotationProcessor;
  }

  public Object apply(ProceedingJoinPoint joinPoint, SagaStart sagaStart, SagaContext context)
      throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    sagaStartAnnotationProcessor.preIntercept(sagaStart.timeout());
    LOG.debug("Saga-Transaction::Initialized context {} before execution of method {}", context, method.toString());
    try {
      Object result = joinPoint.proceed();
      if (sagaStart.autoClose()) {
        sagaStartAnnotationProcessor.postIntercept(context.globalTxId());
        if (LOG.isDebugEnabled())
          LOG.debug("Saga-Transaction::Transaction with context {} has finished.", context);
      } else {
        if (LOG.isDebugEnabled())
          LOG.debug("Saga-Transaction::Transaction with context {} is not finished in the SagaStarted annotated method.", context);
      }
      return result;
    } catch (Throwable throwable) {
      // We don't need to handle the SagaException here
      if (!(throwable instanceof SagaException)) {
        sagaStartAnnotationProcessor.onError(method.toString(), throwable);
        LOG.error("Saga-Transaction::Transaction {} failed.", context.globalTxId());
      }
      throw throwable;
    } finally {
      context.clear();
    }
  }
}
