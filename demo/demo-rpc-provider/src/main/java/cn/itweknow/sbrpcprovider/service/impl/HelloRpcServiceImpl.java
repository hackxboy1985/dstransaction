package cn.itweknow.sbrpcprovider.service.impl;

import cn.ds.transaction.framework.annotations.Compensable;
import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RpcService(HelloRpcService.class)
public class HelloRpcServiceImpl implements HelloRpcService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public HelloRpcServiceImpl(){
//        System.out.println("HelloRpcServiceImpl init");
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


    /**
     * 子事务1方法
     */
    @Transactional
    @Override
    @Compensable(compensationMethod="sayHiRollback")//定义子事务及补偿方法
    public String sayHi(String msg){
        logger.info("sayhi done");
        return "Hi RPC!" + msg;
    }

    /**
     * 子事务2方法
     * @param ex true:抛异常, false:不抛异常
     */
    @Transactional
    @Override
    @Compensable(compensationMethod="sayHelloRollback")//定义子事务及补偿方法
    public String sayHello(String msg, boolean ex) {

        if (ex) {
            logger.info("sayHello exceptioin done");
            throw new RuntimeException("测试exception");
        }else{
            logger.info("sayHello done");
        }
        return "Hello RPC!" + msg;
    }

    /**
     * sayHi的补偿方法
     */
    public String sayHiRollback(String msg) {
        logger.info("sayHi Rollback excute!!");
        return "sayhi Rollback OK! " + msg;
    }

    /**
     * sayHello的补偿方法
     */
    public String sayHelloRollback(String msg, boolean ex) {
        logger.info("sayHello Rollback excute!!");
        return "sayhello Rollback OK! " + msg;
    }


}
