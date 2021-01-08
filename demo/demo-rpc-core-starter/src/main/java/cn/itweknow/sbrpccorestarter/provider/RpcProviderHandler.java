package cn.itweknow.sbrpccorestarter.provider;

import cn.itweknow.sbrpccorestarter.interceptor.RpcResponseInterceptorProcessor;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author sj
 * @date 2020/12/30 11:12
 * @description
 */
public class RpcProviderHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(RpcProviderHandler.class);

    RpcResponseInterceptorProcessor rpcResponseInterceptorProcessor;

    private RpcProviderHandler(){}
    public RpcProviderHandler(RpcResponseInterceptorProcessor rpcResponseInterceptorProcessor){
        this.rpcResponseInterceptorProcessor=rpcResponseInterceptorProcessor;
    }

    public RpcResponse handle(RpcRequest request) throws Exception {
        logger.info("RpcStarter::Provider::server receive request,{}", request);
        // 返回的对象。将请求id原路带回
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(request.getRequestId());
        try {
            if (rpcResponseInterceptorProcessor != null)
                rpcResponseInterceptorProcessor.preIntercept(request.getClassName(),request);
            Object result = invoke(request);
            rpcResponse.setResult(result);
            if (rpcResponseInterceptorProcessor != null)
                rpcResponseInterceptorProcessor.postIntercept(request.getClassName(),request,rpcResponse);
        } catch (Exception e) {
            rpcResponse.setError(e);
            rpcResponse.setMsg(e.getMessage());
        } finally {
            return rpcResponse;
        }
    }


    //TODO:拆出个具体实现类来实现对响应的拦截处理
    private Object invoke(RpcRequest request) throws Exception {
        String className = request.getClassName();
        Class<?> objClz = Class.forName(className);
        Object o = BeanFactory.getBean(objClz);
        // 获取调用的方法名称。
        String methodName = request.getMethodName();
        // 参数类型
        Class<?>[] paramsTypes = request.getParamTypes();
        // 具体参数。
        Object[] params = request.getParams();
        // 调用实现类的指定的方法并返回结果。
        logger.info("RpcStarter::Provider::invoke classname={}, methodName={}",className,methodName);
        Method method = objClz.getMethod(methodName, paramsTypes);
        Object res = method.invoke(o, params);
        return res;
    }
}
