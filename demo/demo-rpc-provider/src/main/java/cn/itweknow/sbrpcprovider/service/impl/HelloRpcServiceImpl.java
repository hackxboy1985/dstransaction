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

    @Override
    @Compensable(compensationMethod="sayHelloRollback")
    public String sayHello(String msg) {
        throw new RuntimeException("exception");
//        return "Hello RPC!" + msg;
    }

    public String sayHiRollback(String msg) {
        return "hi Rollback OK! " + msg;
    }
    public String sayHelloRollback(String msg) {
        return "hello Rollback OK! " + msg;
    }
}
