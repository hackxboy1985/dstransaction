

package cn.ds.transaction.framework.interfaces;

import cn.ds.transaction.framework.AlphaResponse;
import cn.ds.transaction.framework.TxEvent;

public interface SagaMessageSender extends MessageSender {

  AlphaResponse send(TxEvent event);
}
