
package cn.ds.transaction.framework.recovery;

public class RecoveryPolicyFactory {

  //向后恢复-回滚
  private static final RecoveryPolicy DEFAULT_RECOVERY = new DefaultRecovery();

  //向前恢复-重试
  private static final RecoveryPolicy FORWARD_RECOVERY = new ForwardRecovery();

  /**
   * 向前是重试，向后是恢复(回滚)
   * If retries == 0, use the default recovery to execute only once.
   * If retries > 0, it will use the forward recovery and retry the given times at most.
   */
  public static RecoveryPolicy getRecoveryPolicy(int forwardRetries) {
    return forwardRetries > 0 ? FORWARD_RECOVERY : DEFAULT_RECOVERY;
  }
}
