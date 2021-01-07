

package cn.ds.transaction.framework;

/**
 * SagaSvrResponse:saga服务器响应,aborted：是否取消,当SagaSvr落库失败时返回false,否则返回true
 */
public class AlphaResponse {
  private final boolean aborted;

  public AlphaResponse(boolean aborted) {
    this.aborted = aborted;
  }

  public boolean aborted() {
    return aborted;
  }
}
