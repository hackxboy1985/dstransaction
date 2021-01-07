

package cn.ds.transaction.framework.contextHelper;

import cn.ds.transaction.framework.context.OmegaContext;
import cn.ds.transaction.framework.context.TransactionContext;
import cn.ds.transaction.framework.context.TransactionContextProperties;
import org.slf4j.Logger;

public abstract class TransactionContextHelper {

  public TransactionContext extractTransactionContext(Object[] args) {
    if (args != null && args.length > 0) {
      for (Object arg : args) {
        // check the TransactionContext first
        if (arg instanceof TransactionContext) {
          return (TransactionContext) arg;
        }
        if (arg instanceof TransactionContextProperties) {
          TransactionContextProperties transactionContextProperties = (TransactionContextProperties) arg;
          return new TransactionContext(transactionContextProperties.getGlobalTxId(),
              transactionContextProperties.getLocalTxId());
        }
      }
    }
    return null;
  }

  public void populateOmegaContext(OmegaContext context, TransactionContext transactionContext) {
    if (context.globalTxId() != null) {
      getLogger()
          .warn("The context {}'s globalTxId is not empty. Update it for globalTxId:{} and localTxId:{}", context,
              transactionContext.globalTxId(), transactionContext.localTxId());
    } else {
      getLogger()
          .debug("Updated context {} for globalTxId:{} and localTxId:{}", context,
              transactionContext.globalTxId(), transactionContext.localTxId());
    }
    context.setGlobalTxId(transactionContext.globalTxId());
    context.setLocalTxId(transactionContext.localTxId());
  }

  protected abstract Logger getLogger();
}
