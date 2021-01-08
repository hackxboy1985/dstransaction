
package cn.ds.transaction.framework.aspect;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.annotations.SagaEnd;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.SagaAbortedEvent;
import cn.ds.transaction.framework.SagaEndedEvent;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

@Aspect
@Order(value = 300)
public class SagaEndAspect {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final SagaContext context;
  private final SagaMessageSender sender;

  public SagaEndAspect(SagaMessageSender sender, SagaContext context) {
    this.sender = sender;
    this.context = context;
  }

  @Around("execution(@cn.ds.transaction.framework.context.annotations.SagaEnd * *(..)) && @annotation(sagaEnd)")
  Object advise(ProceedingJoinPoint joinPoint, SagaEnd sagaEnd) throws Throwable {
      Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
      try {
        Object result = joinPoint.proceed();
        sendSagaEndedEvent();
        return result;
      } catch (Throwable throwable) {
        // Don't check the SagaException here.
        if (!(throwable instanceof SagaException)) {
          LOG.error("Transaction {} failed.", context.globalTxId());
          sendSagaAbortedEvent(method.toString(), throwable);
        }
        throw throwable;
      }
      finally {
        context.clear();
      }
  }

  private void sendSagaEndedEvent() {
    // TODO need to check the parentID setting
    sender.send(new SagaEndedEvent(context.globalTxId(), context.localTxId()));
  }

  private void sendSagaAbortedEvent(String methodName, Throwable throwable) {
    // TODO need to check the parentID setting
    sender.send(new SagaAbortedEvent(context.globalTxId(), context.localTxId(), null, methodName, throwable));
  }

}
