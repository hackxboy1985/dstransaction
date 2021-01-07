

package cn.ds.transaction.transfer.core;

import com.google.common.base.Supplier;
import java.util.Map;
import cn.ds.transaction.framework.interfaces.MessageSender;

/**
 * The strategy of picking the fastest {@link MessageSender}
 */
public class FastestSender implements MessageSenderPicker {
  @Override
  public MessageSender pick(Map<? extends MessageSender, Long> messageSenders, Supplier<MessageSender> defaultSender) {
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
