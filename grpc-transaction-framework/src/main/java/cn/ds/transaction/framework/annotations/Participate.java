
package cn.ds.transaction.framework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
/**
 * Indicates the annotated method will start a sub-transaction. <br>
 * A <code>@Participate</code> method should satisfy below requirements:
 * <ol>
 *   <li>all parameters are serialized</li>
 *   <li>is idempotent</li>
 *   <li>the object instance which @Participate method resides in should be stateless</li>
 * </ol>
 */
public @interface Participate {
  /**
   * Confirm method name.<br>
   * A confirm method should satisfy below requirements:
   * <ol>
   *   <li>has same parameter list as @Participate method's</li>
   *   <li>all parameters are serialized</li>
   *   <li>is idempotent</li>
   *   <li>be in the same class as @Participate method is in</li>
   * </ol>
   *
   * @return the confirmation method name
   */
  String confirmMethod() default "";

  /**
   * Cancel method name.<br>
   * A cancel method should satisfy below requirements:
   * <ol>
   *   <li>has same parameter list as @Participate method's</li>
   *   <li>all parameters are serialized</li>
   *   <li>is idempotent</li>
   *   <li>be in the same class as @Participate method is in</li>
   * </ol>
   *
   * @return the cancel method name
   */
  String cancelMethod() default "";

}
