
package cn.ds.transaction.framework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Saga事务结束.
 * 一般不使用此注解，除非@SagaStart的autoClose属性设置为false
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface SagaEnd {

}
