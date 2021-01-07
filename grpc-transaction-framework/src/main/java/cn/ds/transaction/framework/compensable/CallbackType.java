

package cn.ds.transaction.framework.compensable;

public enum CallbackType {
  /**
   * Compensation is for the Saga pattern
   */
  Compensation,
  /**
   * Coordination is for the TCC pattern
   */
  Coordination
}
