

package cn.ds.transaction.framework.context;

import java.util.UUID;

public class UniqueIdGenerator implements IdGenerator<String> {
  @Override
  public String nextId() {
    return UUID.randomUUID().toString();
  }
}
