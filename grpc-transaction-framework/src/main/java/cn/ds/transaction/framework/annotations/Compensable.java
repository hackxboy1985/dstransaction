

package cn.ds.transaction.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 子事务注解. <br>
 * 建议使用Spring事务注解来标注该子事务.以使补偿能够能够遵循ACID
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Compensable {

  /**
   * 向前补偿次数.
   * 缺省0，不补偿
   * 值-1, 重试直到成功
   * 值>0, 重试次数
   * 值<-1,抛参数异常IllegalArgumentException
   *
   * @return 向前补偿次数
   */
  int forwardRetries() default 0;

  /**
   * 向后补偿次数.
   * 缺省0，不补偿
   * 值>0, 重试次数
   * 值<0,抛参数异常IllegalArgumentException
   *
   * @return 向后补偿次数
   */
  int reverseRetries() default 0;

  /**
   * 补偿方法名.<br>
   * 补偿方法须满足幂等要求,与事务方法在同一个类且参数须相同且可序列化
   *
   * @return 补偿方法名
   */
  String compensationMethod() default "";

  int retryDelayInMilliseconds() default 5;

  /**
   * 向前补偿超时时间(秒). <br>
   * 缺省0, 永不超时.
   *
   * @return 向前补偿超时时间
   */
  int forwardTimeout() default 0;

  /**
   * 向后补偿超时时间(秒). <br>
   * 缺省0, 永不超时.
   *
   * @return 向后补偿超时时间
   */
  int reverseTimeout() default 0;
}
