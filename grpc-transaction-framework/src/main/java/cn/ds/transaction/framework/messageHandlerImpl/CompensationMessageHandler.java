

package cn.ds.transaction.framework.messageHandlerImpl;

import cn.ds.transaction.framework.CallbackContext;
import cn.ds.transaction.framework.interfaces.MessageHandler;
import cn.ds.transaction.framework.interfaces.SagaMessageSender;
import cn.ds.transaction.framework.TxCompensatedEvent;

/**
 * 补偿消息处理器:交给回调上下文处理（补偿），成功后并发送已补偿方法
 *
 */
public class CompensationMessageHandler implements MessageHandler {

  private final SagaMessageSender sender;

  private final CallbackContext context;

  public CompensationMessageHandler(SagaMessageSender sender, CallbackContext context) {
    this.sender = sender;
    this.context = context;
  }

  @Override
  public void onReceive(String globalTxId, String localTxId, String parentTxId, String compensationMethod,
      Object... payloads) {
    context.apply(globalTxId, localTxId, parentTxId, compensationMethod, payloads);
    if (!context.getSagaContext().getSagaServerMetas().isAkkaEnabled()) {
      sender.send(new TxCompensatedEvent(globalTxId, localTxId, parentTxId, compensationMethod));
    }
  }
}
