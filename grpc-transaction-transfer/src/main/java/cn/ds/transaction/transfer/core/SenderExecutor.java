

package cn.ds.transaction.transfer.core;

import cn.ds.transaction.framework.SagaSvrResponse;

public interface SenderExecutor<T> {

  SagaSvrResponse apply(T event);
}
