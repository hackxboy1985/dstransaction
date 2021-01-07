
package cn.ds.transaction.framework.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates once the annotated method is finished, it will end a saga.
 * Please note:
 *  You need to set the attribute of @SagaStart autoClose to be false，
 *  then you can end the Saga transaction as you want with this SagaEnd annotation.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface SagaEnd {

}
