

package cn.ds.transaction.framework.compensable;

import cn.ds.transaction.framework.annotations.OmegaContextAware;
import cn.ds.transaction.framework.context.OmegaContext;
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

class ExecutorFieldCallback implements FieldCallback {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final OmegaContext omegaContext;
  private final Object bean;

  ExecutorFieldCallback(Object bean, OmegaContext omegaContext) {
    this.omegaContext = omegaContext;
    this.bean = bean;
  }

  @Override
  public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
    if (!field.isAnnotationPresent(OmegaContextAware.class)) {
      return;
    }

    ReflectionUtils.makeAccessible(field);

    Class<?> generic = field.getType();

    if (!Executor.class.isAssignableFrom(generic)) {
      throw new IllegalArgumentException(
          "Only Executor, ExecutorService, and ScheduledExecutorService are supported for @"
              + OmegaContextAware.class.getSimpleName());
    }

    field.set(bean, ExecutorProxy.newInstance(field.get(bean), field.getType(), omegaContext));
  }

  //TODO:线程代理:初始化时将当前线程的globalTxId及localTxId保存至RunnableProxy，
  //TODO:在线程池中执行时，因为是线程池的其它线程，需通过RunnableProxy将globalTxId及localTxId同步到当前线程.
  private static class RunnableProxy implements InvocationHandler {

    private final String globalTxId;
    private final String localTxId;
    private final Object runnable;
    private final OmegaContext omegaContext;

    private static Object newInstance(Object runnable, OmegaContext omegaContext) {
      RunnableProxy runnableProxy = new RunnableProxy(omegaContext, runnable);
      return Proxy.newProxyInstance(
          runnable.getClass().getClassLoader(),
          runnable.getClass().getInterfaces(),
          runnableProxy);
    }

    private RunnableProxy(OmegaContext omegaContext, Object runnable) {
      this.omegaContext = omegaContext;
      this.globalTxId = omegaContext.globalTxId();
      this.localTxId = omegaContext.localTxId();
      this.runnable = runnable;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        LOG.debug("Setting OmegaContext with globalTxId [{}] & localTxId [{}]",
            globalTxId,
            localTxId);

        omegaContext.setGlobalTxId(globalTxId);
        omegaContext.setLocalTxId(localTxId);

        return method.invoke(runnable, args);
      } finally {
        omegaContext.clear();
        LOG.debug("Cleared OmegaContext with globalTxId [{}] & localTxId [{}]",
            globalTxId,
            localTxId);
      }
    }
  }

  //TODO:执行代理
  private static class ExecutorProxy implements InvocationHandler {
    private final Object target;
    private final OmegaContext omegaContext;

    private ExecutorProxy(Object target, OmegaContext omegaContext) {
      this.target = target;
      this.omegaContext = omegaContext;
    }

    private static Object newInstance(Object target, Class<?> targetClass, OmegaContext omegaContext) {
      Class<?>[] interfaces = targetClass.isInterface() ? new Class<?>[] {targetClass} : targetClass.getInterfaces();

      return Proxy.newProxyInstance(
          targetClass.getClassLoader(),
          interfaces,
          new ExecutorProxy(target, omegaContext));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return method.invoke(target, augmentRunnablesWithOmegaContext(args));
    }

    private Object[] augmentRunnablesWithOmegaContext(Object[] args) {
      Object[] augmentedArgs = new Object[args.length];

      for (int i = 0; i < args.length; i++) {
        Object arg = args[i];
        if (isExecutable(arg)) {
          augmentedArgs[i] = RunnableProxy.newInstance(arg, omegaContext);
        } else if (isCollectionOfExecutables(arg)) {
          List argList = new ArrayList();
          Collection argCollection = (Collection<?>) arg;
          for (Object a : argCollection) {
            argList.add(RunnableProxy.newInstance(a, omegaContext));
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
