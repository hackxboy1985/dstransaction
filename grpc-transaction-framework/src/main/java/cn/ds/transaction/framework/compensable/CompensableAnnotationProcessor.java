

package cn.ds.transaction.framework.compensable;

import cn.ds.transaction.framework.CallbackContext;
import cn.ds.transaction.framework.context.OmegaContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

/**
 * 可补偿注解方法增强及OmegaContextAware注解增强：
 * 1、将所有事务方法与补偿方法绑定
 * 2、将OmegaContextAware注解的对象支持异步线程执行时仍然可获得globalTxId及localTxId
 */
public class CompensableAnnotationProcessor implements BeanPostProcessor {

  private final OmegaContext omegaContext;

  private final CallbackContext compensationContext;

  public CompensableAnnotationProcessor(OmegaContext omegaContext, CallbackContext compensationContext) {
    this.omegaContext = omegaContext;
    this.compensationContext = compensationContext;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    checkMethod(bean);
    checkFields(bean);
//    if (beanName.contains("hello")) {
//      System.out.println("beanName="+beanName);
//    }
    return bean;
  }

  private void checkMethod(Object bean) {
    ReflectionUtils.doWithMethods(
        bean.getClass(),
        new CompensableMethodCheckingCallback(bean, compensationContext));
  }

  private void checkFields(Object bean) {
    ReflectionUtils.doWithFields(bean.getClass(), new ExecutorFieldCallback(bean, omegaContext));
  }
}
