package cn.itweknow.sbrpcprovider.service.impl;

import cn.ds.transaction.framework.annotations.Compensable;
import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcService;

@RpcService(HelloRpcService.class)
public class HelloRpcServiceImpl implements HelloRpcService {

    @Compensable(compensationMethod="sayHiRollback")
    @Override
    public String sayHi(String msg){
        return "Hi RPC!" + msg;
    }

    @Compensable(compensationMethod="sayHelloRollback")
    @Override
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
