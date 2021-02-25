package cn.itweknow.sbrpcprovider.service.impl;

import cn.ds.transaction.framework.annotations.Compensable;
import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcService;
import org.springframework.stereotype.Service;

@RpcService(HelloRpcService.class)
public class HelloRpcServiceImpl implements HelloRpcService {

    public HelloRpcServiceImpl(){
//        System.out.println("HelloRpcServiceImpl init");
    }

    @Override
    @Compensable(compensationMethod="sayHiRollback")
    public String sayHi(String msg){
        return "Hi RPC!" + msg;
    }


    int i = 0;
    @Override
    @Compensable(compensationMethod="sayHiRollback", forwardRetries = 2)
    public String sayHiForward(String msg){
        if(i<2){
            i++;
            throw new RuntimeException("test forward");
        }
        return "Hi RPC!" + msg;
    }


    @Override
    @Compensable(compensationMethod="sayHelloRollback")
    public String sayHello(String msg, boolean ex) {
        if (ex) {
            throw new RuntimeException("exception");
        }
        return "Hello RPC!" + msg;
    }

    public String sayHiRollback(String msg) {
        System.out.println("sayHi Rollback excute!!");
        return "sayhi Rollback OK! " + msg;
    }
    public String sayHelloRollback(String msg, boolean ex) {
        return "sayhello Rollback OK! " + msg;
    }
}
