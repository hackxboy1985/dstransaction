package cn.itweknow.sbrpcprovider;

import cn.itweknow.sbrpcapi.service.HelloRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestRpcService {


    @Autowired
    private HelloRpcService helloRpcService;

    public String hello() {
        long start = System.currentTimeMillis();
        String ret="";
        ret = helloRpcService.sayHi("");
        afterSayhi();
        System.out.println("请求耗时:" + (System.currentTimeMillis()-start));
        return ret;
    }

    //test throw exception
    void afterSayhi(){
//        throw new RuntimeException("test");
    }

}
