
package cn.ds.transaction.framework.context;

public class SagaServerMetas {

  private boolean akkaEnabled = false;

  public boolean isAkkaEnabled() {
    return akkaEnabled;
  }

  @Override
  public String toString() {
    return "SagaServerMetas{" +
        "akkaEnabled=" + akkaEnabled +
        '}';
  }

  public static SagaServerMetasBuilder builder() {
    return new SagaServerMetasBuilder();
  }

  public static final class SagaServerMetasBuilder {

    private boolean akkaEnabled = false;

    public SagaServerMetasBuilder akkaEnabled(boolean akkaEnabled) {
      this.akkaEnabled = akkaEnabled;
      return this;
    }

    public SagaServerMetas build() {
      SagaServerMetas sagaServerMetas = new SagaServerMetas();
      sagaServerMetas.akkaEnabled = this.akkaEnabled;
      return sagaServerMetas;
    }
  }
}