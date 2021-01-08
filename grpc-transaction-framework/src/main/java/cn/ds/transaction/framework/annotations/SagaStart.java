

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
   * 自动发送SagaEnded事件给saga服务器.
   * 缺省为true.自动发送，false则不发送
   */
  boolean autoClose() default true;
}
