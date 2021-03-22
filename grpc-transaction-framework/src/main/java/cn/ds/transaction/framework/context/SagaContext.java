
package cn.ds.transaction.framework.context;

/**
 * SagaContext是事务线程上下文，用于串起整个调用链
 */
public class SagaContext {
  public static final String GLOBAL_TX_ID_KEY = "X-SAGA-Global-Transaction-Id";
  public static final String LOCAL_TX_ID_KEY = "X-SAGA-Local-Transaction-Id";

  private final ThreadLocal<String> globalTxId = new InheritableThreadLocal();
  private final ThreadLocal<String> localTxId = new InheritableThreadLocal();
  private final IdGenerator<String> idGenerator;

  private final SagaServerMetas sagaServerMetas;

  public SagaContext(IdGenerator<String> idGenerator) {
    this(idGenerator, SagaServerMetas.builder().akkaEnabled(false).build());
  }

  public SagaContext(IdGenerator<String> idGenerator, SagaServerMetas sagaServerMetas) {
    this.idGenerator = idGenerator;
    this.sagaServerMetas = sagaServerMetas;
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

  public SagaServerMetas getSagaServerMetas() {
    return sagaServerMetas;
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
    return "SagaContext{" +
        "globalTxId=" + globalTxId.get() +
        ", localTxId=" + localTxId.get() +
        ", " + sagaServerMetas +
        '}';
  }

  public void verify(){
    if(this.globalTxId == null){
      throw new RuntimeException("SagaContext globalTxId is empty, Please check if you setup the pack transport handler rightly");
    }
  }
}
