

package cn.ds.transaction.transfer.saga;

import com.google.common.base.Optional;
import cn.ds.transaction.framework.AlphaResponse;

import cn.ds.transaction.transfer.core.LoadBalanceSenderAdapter;
import cn.ds.transaction.transfer.core.MessageSenderPicker;
import cn.ds.transaction.transfer.core.SenderExecutor;
import cn.ds.transaction.transfer.core.LoadBalanceContext;
import cn.ds.transaction.framework.exception.OmegaException;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.TxEvent;

public class SagaLoadBalanceSender extends LoadBalanceSenderAdapter implements SagaMessageSender {

  public SagaLoadBalanceSender(LoadBalanceContext loadContext,
      MessageSenderPicker senderPicker) {
    super(loadContext, senderPicker);
  }

  @Override
  public AlphaResponse send(TxEvent event) {
    do {
      final SagaMessageSender messageSender = pickMessageSender();
      Optional<AlphaResponse> response = doGrpcSend(messageSender, event, new SenderExecutor<TxEvent>() {
        @Override
        public AlphaResponse apply(TxEvent event) {
          return messageSender.send(event);
        }
      });
      if (response.isPresent()) return response.get();
    } while (!Thread.currentThread().isInterrupted());

    throw new OmegaException("Saga-Transaction::Failed to send event " + event + " due to interruption");
  }

//  @Override
  public AlphaResponse send2(TxEvent event) {
    do {
      Optional<AlphaResponse> response = doGrpcSend2(event);
      if (response.isPresent()) return response.get();
    } while (!Thread.currentThread().isInterrupted());

    throw new OmegaException("Saga-Transaction::Failed to send event " + event + " due to interruption");
  }
}
