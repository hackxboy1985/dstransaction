
package cn.ds.transaction.framework.exception;

public class TransactionTimeoutException extends RuntimeException {

  public TransactionTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
