
package cn.ds.transaction.framework.context;

import java.io.Serializable;

/**
 *  This class is holding the Transaction related context which could be use in customer code
 *  , and it is immutable.
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
