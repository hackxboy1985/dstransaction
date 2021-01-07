
package cn.ds.transaction.framework.context;

import java.io.Serializable;

public interface IdGenerator<T extends Serializable> {

  T nextId();
}
