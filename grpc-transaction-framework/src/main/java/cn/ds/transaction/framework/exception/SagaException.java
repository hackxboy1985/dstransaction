

package cn.ds.transaction.framework.exception;

public class SagaException extends RuntimeException {
  public SagaException(String message) {
    super(message);
  }

  public SagaException(Throwable cause) {
    super(cause);
  }

  public SagaException(String cause, Throwable throwable) {
    super(cause, throwable);
  }
}
