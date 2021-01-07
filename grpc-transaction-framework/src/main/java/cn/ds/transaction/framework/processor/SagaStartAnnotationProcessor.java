

package cn.ds.transaction.framework.processor;

import javax.transaction.TransactionalException;

import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.context.OmegaContext;
import cn.ds.transaction.framework.*;
import cn.ds.transaction.framework.exception.OmegaException;

/**
 * saga注解开始处理器
 */
public class SagaStartAnnotationProcessor {

  private final OmegaContext omegaContext;
  private final SagaMessageSender sender;

  public SagaStartAnnotationProcessor(OmegaContext omegaContext, SagaMessageSender sender) {
    this.omegaContext = omegaContext;
    this.sender = sender;
  }

  /**
   *
   * @param timeout
   * @return
   */
  public AlphaResponse preIntercept(int timeout) {
    try {
      return sender
          .send(new SagaStartedEvent(omegaContext.globalTxId(), omegaContext.localTxId(), timeout));
    } catch (OmegaException e) {
      throw new TransactionalException(e.getMessage(), e.getCause());
    }
  }

  public void postIntercept(String parentTxId) {
    AlphaResponse response = sender
        .send(new SagaEndedEvent(omegaContext.globalTxId(), omegaContext.localTxId()));
    //TODO we may know if the transaction is aborted from fsm alpha backend
    if (response.aborted()) {
      throw new OmegaException("transaction " + parentTxId + " is aborted");
    }
  }

  public void onError(String compensationMethod, Throwable throwable) {
    String globalTxId = omegaContext.globalTxId();
    if(omegaContext.getAlphaMetas().isAkkaEnabled()){
      sender.send(
          new SagaAbortedEvent(globalTxId, omegaContext.localTxId(), null, compensationMethod,
              throwable));
    }else{
      sender.send(
          new TxAbortedEvent(globalTxId, omegaContext.localTxId(), null, compensationMethod,
              throwable));
    }
  }
}
