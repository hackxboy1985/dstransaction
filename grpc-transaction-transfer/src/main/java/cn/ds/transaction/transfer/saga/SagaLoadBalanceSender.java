

package cn.ds.transaction.transfer.saga;

import com.google.common.base.Optional;
import cn.ds.transaction.framework.SagaSvrResponse;

import cn.ds.transaction.transfer.core.LoadBalanceSenderAdapter;
import cn.ds.transaction.transfer.core.MessageSenderPicker;
import cn.ds.transaction.transfer.core.SenderExecutor;
import cn.ds.transaction.transfer.core.LoadBalanceContext;
import cn.ds.transaction.framework.exception.SagaException;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.TxEvent;

public class SagaLoadBalanceSender extends LoadBalanceSenderAdapter implements SagaMessageSender {

  public SagaLoadBalanceSender(LoadBalanceContext loadContext,
      MessageSenderPicker senderPicker) {
    super(loadContext, senderPicker);
  }

  @Override
  public SagaSvrResponse send(TxEvent event) {
    do {
      final SagaMessageSender messageSender = pickMessageSender();
      Optional<SagaSvrResponse> response = doGrpcSend(messageSender, event, new SenderExecutor<TxEvent>() {
        @Override
        public SagaSvrResponse apply(TxEvent event) {
          return messageSender.send(event);
        }
      });
      if (response.isPresent()) return response.get();
    } while (!Thread.currentThread().isInterrupted());

    throw new SagaException("Saga-Transaction::Failed to send event " + event + " due to interruption");
  }

//  @Override
//  public SagaSvrResponse send2(TxEvent event) {
//    do {
//      Optional<SagaSvrResponse> response = doGrpcSend2(event);
//      if (response.isPresent()) return response.get();
//    } while (!Thread.currentThread().isInterrupted());
//
//    throw new SagaException("Saga-Transaction::Failed to send event " + event + " due to interruption");
//  }
}
