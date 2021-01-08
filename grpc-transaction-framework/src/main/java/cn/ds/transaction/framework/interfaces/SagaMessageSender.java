

package cn.ds.transaction.framework.interfaces;

import cn.ds.transaction.framework.SagaSvrResponse;
import cn.ds.transaction.framework.TxEvent;

public interface SagaMessageSender extends MessageSender {

  SagaSvrResponse send(TxEvent event);
}
