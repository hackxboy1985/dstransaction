

package cn.ds.transaction.framework.processor;

import javax.transaction.TransactionalException;

import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.context.SagaContext;
import cn.ds.transaction.framework.*;
import cn.ds.transaction.framework.exception.SagaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * saga注解开始处理器
 */
public class SagaStartAnnotationProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(SagaStartAnnotationProcessor.class);


  private final SagaContext sagaContext;
  private final SagaMessageSender sender;

  public SagaStartAnnotationProcessor(SagaContext sagaContext, SagaMessageSender sender) {
    this.sagaContext = sagaContext;
    this.sender = sender;
  }

  /**
   *
   * @param timeout
   * @return
   */
  public SagaSvrResponse preIntercept(int timeout) {
    try {
      return sender
          .send(new SagaStartedEvent(sagaContext.globalTxId(), sagaContext.localTxId(), timeout));
    } catch (SagaException e) {
      throw new TransactionalException(e.getMessage(), e.getCause());
    }
  }

  public void postIntercept(String parentTxId) {
    SagaSvrResponse response = sender
        .send(new SagaEndedEvent(sagaContext.globalTxId(), sagaContext.localTxId()));
    //TODO we may know if the transaction is aborted from fsm SagaSvr backend
    if (response.aborted()) {
      LOG.info("Saga-Transaction:: transaction {} is aborted", parentTxId);
//      throw new SagaException("transaction " + parentTxId + " is aborted");
    }
  }

  public void onError(String compensationMethod, Throwable throwable) {
    String globalTxId = sagaContext.globalTxId();
//    if(sagaContext.getSagaServerMetas().isAkkaEnabled()){
      sender.send(
          new SagaAbortedEvent(globalTxId, sagaContext.localTxId(), null, compensationMethod,
              throwable));
//    }else{
//      sender.send(
//          new TxAbortedEvent(globalTxId, sagaContext.localTxId(), null, compensationMethod,
//              throwable));
//    }
  }
}
