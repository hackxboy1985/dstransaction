
package cn.ds.transaction.framework.context;

import java.io.Serializable;

/**
 *  用户空间可以使用此事务上下文
 */
public class TransactionContext implements Serializable {
  private final String globalTxId;
  private final String localTxId;

  public TransactionContext(String globalTxId, String localTxId) {
    this.globalTxId = globalTxId;
    this.localTxId = localTxId;
  }

  public String globalTxId() {
    return globalTxId;
  }

  public String localTxId() {
    return localTxId;
  }

}
