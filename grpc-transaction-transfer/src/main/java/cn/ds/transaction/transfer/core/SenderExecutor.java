

package cn.ds.transaction.transfer.core;

import cn.ds.transaction.framework.AlphaResponse;

public interface SenderExecutor<T> {

  AlphaResponse apply(T event);
}
