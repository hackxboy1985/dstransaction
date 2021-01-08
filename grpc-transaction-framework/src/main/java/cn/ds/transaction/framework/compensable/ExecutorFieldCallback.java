

package cn.ds.transaction.framework.compensable;

import cn.ds.transaction.framework.annotations.SagaContextAware;
import cn.ds.transaction.framework.context.SagaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * 线程池执行时动态代理，以支持异步线程操作时事务上下文丢失的场景
 */
class ExecutorFieldCallback implements FieldCallback {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final SagaContext sagaContext;
  private final Object bean;

  ExecutorFieldCallback(Object bean, SagaContext sagaContext) {
    this.sagaContext = sagaContext;
    this.bean = bean;
  }

  @Override
  public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
    if (!field.isAnnotationPresent(SagaContextAware.class)) {
      return;
    }

    ReflectionUtils.makeAccessible(field);

    Class<?> generic = field.getType();

    if (!Executor.class.isAssignableFrom(generic)) {
      throw new IllegalArgumentException(
          "Only Executor, ExecutorService, and ScheduledExecutorService are supported for @"
              + SagaContextAware.class.getSimpleName());
    }

    field.set(bean, ExecutorProxy.newInstance(field.get(bean), field.getType(), sagaContext));
  }

  //TODO:线程代理:初始化时将当前线程的globalTxId及localTxId保存至RunnableProxy，
  //TODO:在线程池中执行时，因为是线程池的其它线程，需通过RunnableProxy将globalTxId及localTxId同步到当前线程.
  private static class RunnableProxy implements InvocationHandler {

    private final String globalTxId;
    private final String localTxId;
    private final Object runnable;
    private final SagaContext sagaContext;

    private static Object newInstance(Object runnable, SagaContext sagaContext) {
      RunnableProxy runnableProxy = new RunnableProxy(sagaContext, runnable);
      return Proxy.newProxyInstance(
          runnable.getClass().getClassLoader(),
          runnable.getClass().getInterfaces(),
          runnableProxy);
    }

    private RunnableProxy(SagaContext sagaContext, Object runnable) {
      this.sagaContext = sagaContext;
      this.globalTxId = sagaContext.globalTxId();
      this.localTxId = sagaContext.localTxId();
      this.runnable = runnable;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        LOG.info("Setting SagaContext with globalTxId [{}] & localTxId [{}]",
            globalTxId,
            localTxId);

        sagaContext.setGlobalTxId(globalTxId);
        sagaContext.setLocalTxId(localTxId);

        return method.invoke(runnable, args);
      } finally {
        sagaContext.clear();
        LOG.info("Cleared SagaContext with globalTxId [{}] & localTxId [{}]",
            globalTxId,
            localTxId);
      }
    }
  }

  //TODO:线程执行代理,用于增强方法执行时能够获取事务上下文
  private static class ExecutorProxy implements InvocationHandler {
    private final Object target;
    private final SagaContext sagaContext;

    private ExecutorProxy(Object target, SagaContext sagaContext) {
      this.target = target;
      this.sagaContext = sagaContext;
    }

    private static Object newInstance(Object target, Class<?> targetClass, SagaContext sagaContext) {
      Class<?>[] interfaces = targetClass.isInterface() ? new Class<?>[] {targetClass} : targetClass.getInterfaces();

      return Proxy.newProxyInstance(
          targetClass.getClassLoader(),
          interfaces,
          new ExecutorProxy(target, sagaContext));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return method.invoke(target, augmentRunnablesWithSagaContext(args));
    }

    private Object[] augmentRunnablesWithSagaContext(Object[] args) {
      Object[] augmentedArgs = new Object[args.length];

      for (int i = 0; i < args.length; i++) {
        Object arg = args[i];
        if (isExecutable(arg)) {
          augmentedArgs[i] = RunnableProxy.newInstance(arg, sagaContext);
        } else if (isCollectionOfExecutables(arg)) {
          List argList = new ArrayList();
          Collection argCollection = (Collection<?>) arg;
          for (Object a : argCollection) {
            argList.add(RunnableProxy.newInstance(a, sagaContext));
          }
          augmentedArgs[i] = argList;
        } else {
          augmentedArgs[i] = arg;
        }
      }

      return augmentedArgs;
    }

    private boolean isExecutable(Object arg) {
      return arg instanceof Runnable || arg instanceof Callable;
    }

    private boolean isCollectionOfExecutables(Object arg) {
      return arg instanceof Collection
          && !((Collection<?>) arg).isEmpty()
          && isExecutable(((Collection<?>) arg).iterator().next());
    }
  }
}
