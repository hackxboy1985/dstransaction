

package cn.ds.transaction.transfer.core;

import com.google.common.base.Supplier;
import java.util.Map;
import cn.ds.transaction.framework.interfaces.MessageSender;

/**
 * 挑选消息发送者的策略接口类
 */
public interface MessageSenderPicker {

  /**
   * 从集合中挑选一个MessageSender，如未选中，默认返回defaultSender
   *
   * @param messageSenders 候选集合
   * @param defaultSender 缺省提供者
   * @return MessageSender对象
   */
  MessageSender pick(Map<? extends MessageSender, Long> messageSenders,
      Supplier<MessageSender> defaultSender);
}
