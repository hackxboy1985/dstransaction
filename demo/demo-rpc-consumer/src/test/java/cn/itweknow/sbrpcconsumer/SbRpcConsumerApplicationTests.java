package cn.itweknow.sbrpcconsumer;

import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SbRpcConsumerApplicationTests {

    @RpcConsumer(providerName = "provider")
    private HelloRpcService helloRpcService;

    @Test
    public void contextLoads() {

        for(int i = 0;i<100;i++) {
            long start = System.currentTimeMillis();
            String ret = helloRpcService.sayHello("hello", false);
            System.out.println("请求耗时:" + (System.currentTimeMillis() - start) + ", return=" + ret);
        }
    }

}
