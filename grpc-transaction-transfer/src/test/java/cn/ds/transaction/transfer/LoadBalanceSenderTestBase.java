

package cn.ds.transaction.transfer;

import io.grpc.Server;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class LoadBalanceSenderTestBase {

  protected static final int[] ports = {8080, 8090};

  protected static final Map<Integer, Server> servers = new HashMap<>();

  protected static final Map<Integer, Integer> delays = new HashMap<Integer, Integer>() {{
    put(8080, 0);
    put(8090, 800);
  }};

  protected static final Map<Integer, Queue<String>> connected = new HashMap<Integer, Queue<String>>() {{
    put(8080, new ConcurrentLinkedQueue<String>());
    put(8090, new ConcurrentLinkedQueue<String>());
  }};

  protected static final Map<Integer, Queue<Object>> eventsMap = new HashMap<Integer, Queue<Object>>() {{
    put(8080, new ConcurrentLinkedQueue<>());
    put(8090, new ConcurrentLinkedQueue<>());
  }};
}
