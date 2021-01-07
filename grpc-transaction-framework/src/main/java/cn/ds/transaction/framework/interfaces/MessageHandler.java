

package cn.ds.transaction.framework.interfaces;

/**
 * 消息回调处理器
 */
public interface MessageHandler {
  /**
   * 接收到消息处理函数
   * @param globalTxId
   * @param localTxId
   * @param parentTxId
   * @param compensationMethod
   * @param payloads
   */
  void onReceive(String globalTxId, String localTxId, String parentTxId, String compensationMethod, Object... payloads);
}
