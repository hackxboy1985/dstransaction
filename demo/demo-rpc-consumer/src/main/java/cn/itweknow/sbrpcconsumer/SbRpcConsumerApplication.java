package cn.itweknow.sbrpcconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
//@ComponentScan("cn.itweknow.*")
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class SbRpcConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbRpcConsumerApplication.class, args);
    }

}
