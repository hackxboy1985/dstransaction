
package cn.ds.transaction.framework.context;

/**
 * OmegaContext holds the globalTxId and localTxId which are used to build the invocation map
 */
public class OmegaContext {
  public static final String GLOBAL_TX_ID_KEY = "X-Pack-Global-Transaction-Id";
  public static final String LOCAL_TX_ID_KEY = "X-Pack-Local-Transaction-Id";

  private final ThreadLocal<String> globalTxId = new InheritableThreadLocal();
  private final ThreadLocal<String> localTxId = new InheritableThreadLocal();
  private final IdGenerator<String> idGenerator;

  private final AlphaMetas alphaMetas;

  public OmegaContext(IdGenerator<String> idGenerator) {
    this(idGenerator, AlphaMetas.builder().akkaEnabled(false).build());
  }

  public OmegaContext(IdGenerator<String> idGenerator, AlphaMetas alphaMetas) {
    this.idGenerator = idGenerator;
    this.alphaMetas = alphaMetas;
  }

  public String newGlobalTxId() {
    String id = idGenerator.nextId();
    globalTxId.set(id);
    return id;
  }

  public void setGlobalTxId(String txId) {
    globalTxId.set(txId);
  }

  public String globalTxId() {
    return globalTxId.get();
  }

  public String newLocalTxId() {
    String id = idGenerator.nextId();
    localTxId.set(id);
    return id;
  }

  public void setLocalTxId(String localTxId) {
    this.localTxId.set(localTxId);
  }

  public String localTxId() {
    return localTxId.get();
  }

  public AlphaMetas getAlphaMetas() {
    return alphaMetas;
  }

  public TransactionContext getTransactionContext() {
    return new TransactionContext(globalTxId(), localTxId());
  }

  public void clear() {
    globalTxId.remove();
    localTxId.remove();
  }

  @Override
  public String toString() {
    return "OmegaContext{" +
        "globalTxId=" + globalTxId.get() +
        ", localTxId=" + localTxId.get() +
        ", " + alphaMetas +
        '}';
  }

  public void verify(){
    if(this.globalTxId == null){
      throw new RuntimeException("OmegaContext globalTxId is empty, Please check if you setup the pack transport handler rightly");
    }
  }
}
