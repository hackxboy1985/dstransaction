

package cn.ds.transaction.framework.compensable;

import cn.ds.transaction.framework.CallbackContext;
import cn.ds.transaction.framework.exception.OmegaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class MethodCheckingCallback implements MethodCallback {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Object bean;

  private final CallbackContext callbackContext;

  private final CallbackType callbackType;

  public MethodCheckingCallback(Object bean, CallbackContext callbackContext, CallbackType callbackType) {
    this.bean = bean;
    this.callbackContext = callbackContext;
    this.callbackType = callbackType;
  }

  protected void loadMethodContext(Method method, String ... candidates) {
    for (String each : candidates) {
      try {
        //TODO:将事务方法与补偿方法进行关联并存储至callbackContext
        Method signature = bean.getClass().getDeclaredMethod(each, method.getParameterTypes());
        String key = getTargetBean(bean).getClass().getDeclaredMethod(each, method.getParameterTypes()).toString();
        callbackContext.addCallbackContext(key, signature, bean);

        LOG.info("Saga-Transaction::Found compensable method [{}] in {}", each, bean.getClass().getCanonicalName());
      } catch (Exception ex) {
        throw new OmegaException(
            "No such " + callbackType + " method [" + each + "] found in " + bean.getClass().getCanonicalName(), ex);
      }
    }
  }

  private Object getTargetBean(Object proxy) throws Exception {
    if(!AopUtils.isAopProxy(proxy)) {
      return proxy;
    }

    if(AopUtils.isJdkDynamicProxy(proxy)) {
      return getJdkDynamicProxyTargetObject(proxy);
    } else {
      return getCglibProxyTargetObject(proxy);
    }
  }

  private Object getCglibProxyTargetObject(Object proxy) throws Exception {
    Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
    h.setAccessible(true);
    Object dynamicAdvisedInterceptor = h.get(proxy);

    Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
    advised.setAccessible(true);

    Object result = ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
    return result;
  }


  private Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
    Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
    h.setAccessible(true);
    AopProxy aopProxy = (AopProxy) h.get(proxy);

    Field advised = aopProxy.getClass().getDeclaredField("advised");
    advised.setAccessible(true);

    Object result = ((AdvisedSupport)advised.get(aopProxy)).getTargetSource().getTarget();
    return result;
  }
}
