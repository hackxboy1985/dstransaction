

package cn.ds.transaction.framework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标示Saga事务的开始.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface SagaStart {

  /**
   * Saga超时时间(秒). <br>
   * 缺省0, 永不超时.
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
