

package cn.ds.transaction.framework.aspect;

import cn.ds.transaction.framework.wrapper.SagaStartAnnotationProcessorTimeoutWrapper;
import cn.ds.transaction.framework.wrapper.SagaStartAnnotationProcessorWrapper;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.annotations.SagaStart;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.processor.SagaStartAnnotationProcessor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

@Aspect
@Order(value = 100)
public class SagaStartAspect {

  private final SagaStartAnnotationProcessor sagaStartAnnotationProcessor;

  private final SagaContext context;

  public SagaStartAspect(SagaMessageSender sender, SagaContext context) {
    this.context = context;
    this.sagaStartAnnotationProcessor = new SagaStartAnnotationProcessor(context, sender);
  }

  @Around("execution(@cn.ds.transaction.framework.annotations.SagaStart * *(..)) && @annotation(sagaStart)")
  Object advise(ProceedingJoinPoint joinPoint, SagaStart sagaStart) throws Throwable {
    initializeSagaContext();
    if(context.getSagaServerMetas().isAkkaEnabled() && sagaStart.timeout()>0){
      SagaStartAnnotationProcessorTimeoutWrapper wrapper = new SagaStartAnnotationProcessorTimeoutWrapper(this.sagaStartAnnotationProcessor);
      return wrapper.apply(joinPoint,sagaStart,context);
    }else{
      SagaStartAnnotationProcessorWrapper wrapper = new SagaStartAnnotationProcessorWrapper(this.sagaStartAnnotationProcessor);
      return wrapper.apply(joinPoint,sagaStart,context);
    }
  }

  private void initializeSagaContext() {
    context.setLocalTxId(context.newGlobalTxId());
  }
}
