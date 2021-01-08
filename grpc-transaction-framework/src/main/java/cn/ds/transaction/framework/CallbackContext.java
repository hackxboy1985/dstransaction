

package cn.ds.transaction.framework;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.context.SagaContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackContext {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Map<String, CallbackContextInternal> contexts = new ConcurrentHashMap<>();
  private final SagaContext sagaContext;
  private final SagaMessageSender sender;

  public CallbackContext(SagaContext sagaContext, SagaMessageSender sender) {
    this.sagaContext = sagaContext;
    this.sender = sender;
  }

  /**
   * 将事务方法对应的补偿方法加入到回调上下文存储
   * @param key 事务方法
   * @param compensationMethod 补偿方法
   * @param target bean
   */
  public void addCallbackContext(String key, Method compensationMethod, Object target) {
//    LOG.info("Saga-Transaction::compensation method register {}=",key);
    compensationMethod.setAccessible(true);
    if (!contexts.containsKey(key)) {
      contexts.put(key, new CallbackContextInternal(target, compensationMethod));
    }
  }

  /**
   * 回调上下文进行申请补偿
   * @param globalTxId
   * @param localTxId
   * @param parentTxId
   * @param callbackMethod
   * @param payloads
   */
  public void apply(String globalTxId, String localTxId, String parentTxId, String callbackMethod, Object... payloads) {
    String oldGlobalTxId = sagaContext.globalTxId();
    String oldLocalTxId = sagaContext.localTxId();
    try {
      sagaContext.setGlobalTxId(globalTxId);
      sagaContext.setLocalTxId(localTxId);
      if (contexts.containsKey(callbackMethod)) {
        //TODO:补偿
        LOG.info("Saga-Transaction::Compensate:Callback transaction [start] with global tx id [{}], local tx id [{}], callbackMethod[{}]", globalTxId, localTxId, callbackMethod);
        CallbackContextInternal contextInternal = contexts.get(callbackMethod);
        contextInternal.callbackMethod.invoke(contextInternal.target, payloads);
        if (sagaContext.getSagaServerMetas().isAkkaEnabled()) {
          sender.send(
              new TxCompensateAckSucceedEvent(sagaContext.globalTxId(), sagaContext.localTxId(),
                  parentTxId, callbackMethod));
        }
        LOG.info("Saga-Transaction::Compensate:Callback transaction [succe] with global tx id [{}], local tx id [{}], callbackMethod[{}]", globalTxId, localTxId, callbackMethod);
      } else {
        if (sagaContext.getSagaServerMetas().isAkkaEnabled()) {
          String msg = "Saga-Transaction::Compensate:callback method " + callbackMethod
              + " not found on CallbackContext, If it is starting, please try again later";
          sender.send(
              new TxCompensateAckFailedEvent(sagaContext.globalTxId(), sagaContext.localTxId(),
                  parentTxId, callbackMethod, new Exception(msg)));
          LOG.error(msg);
          LOG.error("Saga-Transaction::Compensate:Callback transaction [notfound] with global tx id [{}], local tx id [{}], callbackMethod[{}]", globalTxId, localTxId, callbackMethod);

        }else{
          throw new NullPointerException();
        }
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      if (sagaContext.getSagaServerMetas().isAkkaEnabled()) {
        sender.send(
            new TxCompensateAckFailedEvent(sagaContext.globalTxId(), sagaContext.localTxId(),
                parentTxId, callbackMethod, e));
      }
      LOG.error("Saga-Transaction::Compensate:Callback transaction [error] with global tx id [{}], local tx id [{}], callbackMethod[{}]", globalTxId, localTxId, callbackMethod);
      LOG.error(
          "Pre-checking for callback method " + callbackMethod
              + " was somehow skipped, did you forget to configure callback method checking on service startup?",
          e);
    } finally {
      sagaContext.setGlobalTxId(oldGlobalTxId);
      sagaContext.setLocalTxId(oldLocalTxId);
    }
  }

  public SagaContext getSagaContext() {
    return sagaContext;
  }

  private static final class CallbackContextInternal {
    private final Object target;

    private final Method callbackMethod;

    private CallbackContextInternal(Object target, Method callbackMethod) {
      this.target = target;
      this.callbackMethod = callbackMethod;
    }
  }
}
