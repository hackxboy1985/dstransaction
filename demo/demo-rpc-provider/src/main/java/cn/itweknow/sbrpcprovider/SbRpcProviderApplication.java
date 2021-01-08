package cn.itweknow.sbrpcprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass=true)
//@ComponentScan("cn.itweknow.*")
public class SbRpcProviderApplication {


    public static void main(String[] args) {
        SpringApplication.run(SbRpcProviderApplication.class, args);
    }

}
