package cn.ds.transaction.transfer.core;

import com.google.common.base.Supplier;
import java.util.Map;
import cn.ds.transaction.framework.interfaces.MessageSender;

/**
 * 最快响应算法
 */
public class FastestSender implements MessageSenderPicker {

  @Override
  public MessageSender pick(Map<? extends MessageSender, Long> messageSenders, Supplier<MessageSender> defaultSender) {
    //Map的value为上一次响应时间，本算法寻找最快的响应的消息发送者
    Long min = Long.MAX_VALUE;
    MessageSender sender = null;
    for (Map.Entry<? extends MessageSender, Long> entry : messageSenders.entrySet()) {
      if (entry.getValue() != Long.MAX_VALUE && min > entry.getValue()) {
        min = entry.getValue();
        sender = entry.getKey();
      }
    }
    if (sender == null) {
      return defaultSender.get();
    } else {
      return sender;
    }
  }
}
