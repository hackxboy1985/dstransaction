

package cn.ds.transaction.transfer.core;

import com.google.common.base.Supplier;
import java.util.Collection;
import java.util.Map;
import cn.ds.transaction.framework.interfaces.MessageSender;

/**
 * The strategy of picking a specific {@link MessageSender} from a {@link Collection} of {@link
 * MessageSender}s
 */
public interface MessageSenderPicker {

  /**
   * Pick one from the Collection. Return default sender if none is picked.
   *
   * @param messageSenders Candidates map, the Key Set of which is the collection of candidate
   * senders.
   * @param defaultSender Default sender provider
   * @return The specified one.
   */
  MessageSender pick(Map<? extends MessageSender, Long> messageSenders,
      Supplier<MessageSender> defaultSender);
}
