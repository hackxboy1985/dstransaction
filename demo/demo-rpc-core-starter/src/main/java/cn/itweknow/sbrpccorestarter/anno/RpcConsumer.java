package cn.itweknow.sbrpccorestarter.anno;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author sj
 * @date 2020/12/26 17:08
 * @description
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RpcConsumer {

    /**
     * 服务名称
     * @return
     */
    String providerName();

}
