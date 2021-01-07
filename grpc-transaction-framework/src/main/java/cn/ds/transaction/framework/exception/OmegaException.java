

package cn.ds.transaction.framework.exception;

public class OmegaException extends RuntimeException {
  public OmegaException(String message) {
    super(message);
  }

  public OmegaException(Throwable cause) {
    super(cause);
  }

  public OmegaException(String cause, Throwable throwable) {
    super(cause, throwable);
  }
}
