package cn.itweknow.sbrpcapi.service;



/**
 * @author sj
 * @date 2020/12/26 14:07
 * @description
 */
public interface HelloRpcService {

    String sayHi(String msg);

    String sayHiForward(String msg);

    String sayHello(String msg, boolean ex);

}
