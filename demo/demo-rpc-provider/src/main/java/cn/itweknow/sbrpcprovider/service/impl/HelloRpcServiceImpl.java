package cn.itweknow.sbrpcprovider.service.impl;

import cn.ds.transaction.framework.annotations.Compensable;
import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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



    private Map<String, Boolean> planeBookings = new ConcurrentHashMap();
    private Map<String, Boolean> hotelBookings = new ConcurrentHashMap();

    /**
     * 飞机预订
     * @param user
     */
    @Transactional
    @Override
    @Compensable(compensationMethod="planeBookingRollback")//定义子事务及补偿方法
    public String planeBooking(String user){
        planeBookings.put(user,Boolean.TRUE);
        logger.info(user+"飞机票预订成功");
        return user+"飞机票预订成功";
    }
    /**
     * 飞机预订补偿方法
     */
    public String planeBookingRollback(String user){
        if (planeBookings.containsKey(user)) {
            planeBookings.remove(user);
        }
        logger.info(user+"飞机票预订已取消");
        return "飞机票已取消预订";
    }
    /**
     * 酒店预订
     * @param user
     */
    @Transactional
    @Override
    @Compensable(compensationMethod="hotelBookingRollback")//定义子事务及补偿方法
    public String hotelBooking(String user, int peopleNum){
        if (peopleNum >= 2){
            logger.info(user+"酒店预订失败:酒店已满");
            throw new RuntimeException("酒店已满");
        }else{
            hotelBookings.put(user,Boolean.TRUE);
            logger.info(user+"酒店预订成功");
            return user+"酒店预订成功";
        }
    }
    /**
     * 酒店预订补偿方法
     */
    public String hotelBookingRollback(String user, int peopleNum){
        if (hotelBookings.containsKey(user))
            hotelBookings.remove(user);
        logger.info(user+"酒店预订已取消");
        return "酒店已取消预订";
    }


}


