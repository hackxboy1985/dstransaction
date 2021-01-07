

package cn.ds.transaction.framework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates the annotated method will start a saga.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface SagaStart {

  /**
   * Saga timeout, in seconds. <br>
   * Default value is 0, which means never timeout.
   *
   * @return
   */
  int timeout() default 0;

  /**
   * Sending out SagaEnded event to Alpha once the SagaStart annotated method is finished without any error.
   * Default value is true, which means Omega sends out the SagaEnded event to Alpha once the annotated method is finished.
   * Value is false, which means Omega never sends out the SagaEnded event to Alpha once the annotated method is finished.
   */
  boolean autoClose() default true;
}
