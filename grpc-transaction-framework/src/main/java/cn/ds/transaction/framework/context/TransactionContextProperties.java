
package cn.ds.transaction.framework.context;

/**
 * 事务上下文属性接口,子事务执行前将可通过此接口获得事务上下文.
 */
public interface TransactionContextProperties {
  String getGlobalTxId();
  String getLocalTxId();

}
