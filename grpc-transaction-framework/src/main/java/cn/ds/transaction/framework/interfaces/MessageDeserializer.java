

package cn.ds.transaction.framework.interfaces;

public interface MessageDeserializer {
  Object[] deserialize(byte[] message);
}
