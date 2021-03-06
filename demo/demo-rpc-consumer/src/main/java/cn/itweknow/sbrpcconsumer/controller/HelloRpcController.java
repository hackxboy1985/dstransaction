package cn.itweknow.sbrpcconsumer.controller;

import cn.ds.transaction.framework.annotations.SagaStart;
import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcConsumer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello-rpc")
public class HelloRpcController {


    @RpcConsumer(providerName = "provider")
    private HelloRpcService helloRpcService;

    //TODO:测试向后补偿
    @SagaStart
    @GetMapping("/hi")
    public String hello(@RequestParam String msg) {
        long start = System.currentTimeMillis();
        String ret="";
        ret = helloRpcService.sayHi(msg);
        afterSayhi();
        System.out.println("请求耗时:" + (System.currentTimeMillis()-start));
        return ret;
    }

    //TODO:测试向前补偿
    @SagaStart
    @GetMapping("/hiForward")
    public String sayHiForward(@RequestParam String msg) {
        long start = System.currentTimeMillis();
        String ret="";
        ret = helloRpcService.sayHiForward(msg);
        afterSayhi();
        System.out.println("请求耗时:" + (System.currentTimeMillis()-start));
        return ret;
    }

    //test throw exception
    void afterSayhi(){
        throw new RuntimeException("test");
    }

    @GetMapping("/hello")
    public String helloRpcTx(@RequestParam String msg) {
        System.out.println("请求hello start");
        long start = System.currentTimeMillis();
        String ret = "";
        try {

            ret = helloRpcService.sayHello(msg);
            System.out.println("请求耗时:" + (System.currentTimeMillis() - start));
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }
}
