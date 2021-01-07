
package cn.ds.transaction.framework.context;

/**
 * Once the user business class implement this TransactionContextWrapper, Omega could extract the GlobalTransactionId
 * and LocalTransactionId out of the business class, and set up the OmegaContext before calling sub transaction method.
 */
public interface TransactionContextProperties {
  String getGlobalTxId();
  String getLocalTxId();

}
