
package cn.ds.transaction.framework.context;

public class AlphaMetas {

  private boolean akkaEnabled = false;

  public boolean isAkkaEnabled() {
    return akkaEnabled;
  }

  @Override
  public String toString() {
    return "AlphaMetas{" +
        "akkaEnabled=" + akkaEnabled +
        '}';
  }

  public static AlphaMetasBuilder builder() {
    return new AlphaMetasBuilder();
  }

  public static final class AlphaMetasBuilder {

    private boolean akkaEnabled = false;

    public AlphaMetasBuilder akkaEnabled(boolean akkaEnabled) {
      this.akkaEnabled = akkaEnabled;
      return this;
    }

    public AlphaMetas build() {
      AlphaMetas alphaMetas = new AlphaMetas();
      alphaMetas.akkaEnabled = this.akkaEnabled;
      return alphaMetas;
    }
  }
}