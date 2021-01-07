

package cn.ds.transaction.framework.wrapper;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import cn.ds.transaction.framework.context.OmegaContext;
import cn.ds.transaction.framework.annotations.SagaStart;
import cn.ds.transaction.framework.exception.OmegaException;
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

  public Object apply(ProceedingJoinPoint joinPoint, SagaStart sagaStart, OmegaContext context)
      throws Throwable {
    Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
    sagaStartAnnotationProcessor.preIntercept(sagaStart.timeout());
    LOG.debug("Initialized context {} before execution of method {}", context, method.toString());
    try {
      Object result = joinPoint.proceed();
      if (sagaStart.autoClose()) {
        sagaStartAnnotationProcessor.postIntercept(context.globalTxId());
        LOG.debug("Transaction with context {} has finished.", context);
      } else {
        LOG.debug("Transaction with context {} is not finished in the SagaStarted annotated method.", context);
      }
      return result;
    } catch (Throwable throwable) {
      // We don't need to handle the OmegaException here
      if (!(throwable instanceof OmegaException)) {
        sagaStartAnnotationProcessor.onError(method.toString(), throwable);
        LOG.error("Transaction {} failed.", context.globalTxId());
      }
      throw throwable;
    } finally {
      context.clear();
    }
  }
}
