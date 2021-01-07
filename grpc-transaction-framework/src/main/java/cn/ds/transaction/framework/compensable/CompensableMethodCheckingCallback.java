

package cn.ds.transaction.framework.compensable;

import cn.ds.transaction.framework.CallbackContext;
import cn.ds.transaction.framework.annotations.Compensable;

import java.lang.reflect.Method;

/**
 * 将事务方法与补偿方法绑定装载至callbackContext当中
 */
class CompensableMethodCheckingCallback extends MethodCheckingCallback {

  public CompensableMethodCheckingCallback(Object bean, CallbackContext callbackContext) {
    super(bean, callbackContext, CallbackType.Compensation);
  }

  @Override
  public void doWith(Method method) throws IllegalArgumentException {
    if (!method.isAnnotationPresent(Compensable.class)) {
      return;
    }
    Compensable compensable = method.getAnnotation(Compensable.class);
    String compensationMethod = compensable.compensationMethod();
    // we don't support the retries number below -1.
    if (compensable.forwardRetries() < -1) {
      throw new IllegalArgumentException(String.format("Compensable %s of method %s, the forward retries should not below -1.", compensable, method.getName()));
    }
    loadMethodContext(method, compensationMethod);
  }
}
