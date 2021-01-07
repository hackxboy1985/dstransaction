

package cn.ds.transaction.framework.interfaces;


import cn.ds.transaction.grpc.protocol.ServerMeta;

/**
 * Saga消息发送接口
 */
public interface MessageSender {

  /**
   * 连接后回调
   */
  void onConnected();

  /**
   * 断开连接后回调
   */
  void onDisconnected();

  /**
   * 获得服务器元信息
   */
  ServerMeta onGetServerMeta();

  /**
   * 关闭
   */
  void close();

  /**
   * 目标地址(服务器）
   * @return
   */
  String target();

}
