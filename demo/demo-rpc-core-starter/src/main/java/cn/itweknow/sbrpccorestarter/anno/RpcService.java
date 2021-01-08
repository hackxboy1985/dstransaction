package cn.itweknow.sbrpccorestarter.anno;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * @author sj
 * @date 2020/12/26 14:03
 * @description
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@Documented
public @interface RpcService {

    Class<?> value();

}
