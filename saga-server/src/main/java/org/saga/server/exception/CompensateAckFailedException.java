

package org.saga.server.exception;

public class CompensateAckFailedException extends RuntimeException {
  public CompensateAckFailedException(String cause) {
    super(cause);
  }
}