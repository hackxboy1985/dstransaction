package cn.itweknow.sbrpccorestarter.consumer;


import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author sj
 * @date 2020/12/26 17:11
 * @description
 */
//@Component
public class RpcProxy {

    private static final Logger logger = LoggerFactory.getLogger(RpcProxy.class);


    @Autowired
    private RpcInvoker rpcInvoker;

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass, String providerName) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {

                    //TODO:ignore  method of baseclass
                    String interfaceClassName = interfaceClass.getName();
                    String metnodClassName = method.getDeclaringClass().getName();
                    if (interfaceClassName.equals(metnodClassName) == false) {
                        //System.out.println("invoke proxy baseclass method =" + method);
                        return "";
                    }
//                    System.out.println("invoke proxy invoke! method=" + method);

                    // 构建一个请求。通过netty向Rpc服务发送请求。
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString())
                            .setClassName(method.getDeclaringClass().getName())
                            .setMethodName(method.getName())
                            .setParamTypes(method.getParameterTypes())
                            .setParams(args);

                    try {
                        //logger.info("发起rpc调用");
                        RpcResponse response = rpcInvoker.invoke(providerName, request);
                        if (response.isError()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        throw e;
                    }
                });
    }

}
