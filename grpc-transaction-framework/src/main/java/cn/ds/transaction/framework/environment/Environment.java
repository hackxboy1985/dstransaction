

package cn.ds.transaction.framework.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Environment {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static Environment instance = new Environment();
  private static final int PAYLOADS_MAX_LENGTH = 10240;
  private int payloadsMaxLength = 0;

  public Environment() {
    if (payloadsMaxLength == 0) {
      String val = System.getenv("PAYLOADS_MAX_LENGTH");
      if (val == null || val.trim().length() == 0) {
        payloadsMaxLength = PAYLOADS_MAX_LENGTH;
      } else {
        try {
          payloadsMaxLength = Integer.parseInt(val);
        } catch (NumberFormatException ex) {
          payloadsMaxLength = PAYLOADS_MAX_LENGTH;
          LOG.error(
              "Failed to parse environment variable PAYLOADS_MAX_LENGTH={}, use default value {}",
              val, PAYLOADS_MAX_LENGTH);
        }
      }
    }
  }

  public static Environment getInstance(){
    return instance;
  }

  public int getPayloadsMaxLength() {
    return this.payloadsMaxLength;
  }
}
