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

    /**
     * 飞机预订
     * @param user
     * @return
     */
    String planeBooking(String user);

    /**
     * 酒店预订成功
     * @param user
     * @return
     */
    String hotelBooking(String user, int peopleNum);
}
