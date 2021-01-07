

package cn.ds.transaction.framework.messageFormat;

import cn.ds.transaction.framework.exception.OmegaException;
import cn.ds.transaction.framework.interfaces.MessageFormat;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

import java.io.ByteArrayInputStream;

public class KryoMessageFormat implements MessageFormat {

  private static final int DEFAULT_BUFFER_SIZE = 4096;

  private static final KryoFactory factory = new KryoFactory() {
    @Override
    public Kryo create() {
      return new Kryo();
    }
  };

  private static final KryoPool pool = new KryoPool.Builder(factory).softReferences().build();

  @Override
  public byte[] serialize(Object[] objects) {
    Output output = new Output(DEFAULT_BUFFER_SIZE, -1);

    Kryo kryo = pool.borrow();
    kryo.writeObjectOrNull(output, objects, Object[].class);
    pool.release(kryo);

    return output.toBytes();
  }

  @Override
  public Object[] deserialize(byte[] message) {
    try {
      Input input = new Input(new ByteArrayInputStream(message));

      Kryo kryo = pool.borrow();
      Object[] objects = kryo.readObjectOrNull(input, Object[].class);
      pool.release(kryo);

      return objects;
    } catch (KryoException e) {
      throw new OmegaException("Unable to deserialize message", e);
    }
  }
}
