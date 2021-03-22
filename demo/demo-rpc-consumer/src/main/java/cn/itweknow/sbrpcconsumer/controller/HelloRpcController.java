package cn.itweknow.sbrpcconsumer.controller;

import cn.ds.transaction.framework.annotations.SagaStart;
import cn.itweknow.sbrpcapi.service.HelloRpcService;
import cn.itweknow.sbrpccorestarter.anno.RpcConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rpc")
public class HelloRpcController {
    private Logger logger = LoggerFactory.getLogger(getClass());


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

            ret = helloRpcService.sayHello(msg,false);
            System.out.println("请求耗时:" + (System.currentTimeMillis() - start));
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }


    /**
     * 成功场景:分布式事务包含2个子事务，各子事务均执行成功
     * @param msg 入参
     * @return 响应
     */
    @SagaStart //定义分布式事务开始与自动结束
    @GetMapping("/test2TransactionSucc")
    public String t2succ(@RequestParam String msg) {
        logger.info("测试成功场景，2个子事务 start");
        try {
            long start = System.currentTimeMillis();
            logger.info("子事务1 start");
            helloRpcService.sayHi(msg);
            logger.info("子事务1 end");
            logger.info("子事务2 start");
            String ret = helloRpcService.sayHello(msg,false);//false不抛异常
            logger.info("子事务2 end");
            logger.info("事务耗时:" + (System.currentTimeMillis() - start));
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * 异常场景:分布式事务包含2个子事务，第2子处事抛异常，执行事务回滚补偿
     * @param msg 入参
     * @return 响应
     */
    @SagaStart
    @GetMapping("/test2TransactionError")
    public String t2err(@RequestParam String msg) {
        logger.info("测试异常场景，2个子事务 start");

        try {
            long start = System.currentTimeMillis();
            logger.info("子事务1 start");
            helloRpcService.sayHi(msg);
            logger.info("子事务1 end");
            logger.info("子事务2 start");
            String ret = helloRpcService.sayHello(msg,true);//true不抛异常
            logger.info("子事务2 end");
            logger.info("事务耗时:" + (System.currentTimeMillis() - start));
            return ret;
        }catch (Exception e){
            //e.printStackTrace();
            return "error";
        }
    }


    /**
     * 预订订单:分布式事务包含2个子事务(预订飞机、预订酒店)
     * @param user 用户名
     * @param peopleNum 人数
     * @return 响应
     */
    @SagaStart
    @GetMapping("/order")
    public String order(@RequestParam String user,@RequestParam int peopleNum) {
        logger.info("预订订单场景，2个子事务");
        long start = System.currentTimeMillis();
        try {
            logger.info("子事务1-预订飞机 开始");
            helloRpcService.planeBooking(user);
            logger.info("子事务1-预订飞机 结束");

            logger.info("子事务2-预订酒店 开始");
            String ret = helloRpcService.hotelBooking(user,peopleNum);
            logger.info("子事务2-预订酒店 结束");
            logger.info("预订订单成功-事务耗时:" + (System.currentTimeMillis() - start));
            return user+"预订成功";
        }catch (Exception e){
            logger.info("预订订单异常-事务耗时:" + (System.currentTimeMillis() - start));
            return user+"预订失败:"+e.getMessage();
        }
    }

//    /**
//     * 预订异常场景:分布式事务包含2个子事务(预订飞机、预订酒店)，预订酒店事务抛异常，执行事务回滚补偿
//     * @param user 入参用户
//     * @return 响应
//     */
//    @SagaStart
//    @GetMapping("/orderErr")
//    public String orderErr(@RequestParam String user) {
//        logger.info("测试预订订单失败场景，2个子事务 start");
//        try {
//            long start = System.currentTimeMillis();
//            logger.info("子事务1-预订飞机 开始");
//            helloRpcService.planeBooking(user);
//            logger.info("子事务1-预订飞机 结束");
//
//            logger.info("子事务2-预订酒店 start");
//            String ret = helloRpcService.hotelBooking(user,2);
//            logger.info("子事务2-预订酒店 结束");
//            logger.info("事务耗时:" + (System.currentTimeMillis() - start));
//            return ret;
//        }catch (Exception e){
//            e.printStackTrace();
//            return "error";
//        }
//    }

}
