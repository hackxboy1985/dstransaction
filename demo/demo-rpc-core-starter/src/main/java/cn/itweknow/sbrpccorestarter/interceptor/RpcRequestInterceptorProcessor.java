package cn.itweknow.sbrpccorestarter.interceptor;

import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * RpcRequestInterceptor的处理器
 */
public class RpcRequestInterceptorProcessor  {

    private Logger logger = LoggerFactory.getLogger(getClass());


    RpcRequestInterceptorHolder rpcRequestInterceptorHolder;

    public RpcRequestInterceptorProcessor(RpcRequestInterceptorHolder requestInterceptorHolder){
        this.rpcRequestInterceptorHolder = requestInterceptorHolder;
    }

    public void preIntercept(String providerName, RpcRequest request){
        if (CollectionUtils.isEmpty(rpcRequestInterceptorHolder.getInterceptorList()) == false){
            List<RpcInvokeInterceptor> interceptorList = rpcRequestInterceptorHolder.getInterceptorList();
            for (RpcInvokeInterceptor<RpcRequest,RpcResponse> interceptor : interceptorList){
                try {
                    interceptor.preIntercept(providerName, request);
                }catch (Exception e){
                    logger.error("RpcStarter::Request: preIntercept[{}] error:",interceptor.getClass(),e.getMessage(),e);
                }
            }
        }
    }

    public void postIntercept(String providerName, RpcRequest request, RpcResponse response){
        if (CollectionUtils.isEmpty(rpcRequestInterceptorHolder.getInterceptorList()) == false){
            List<RpcInvokeInterceptor> interceptorList = rpcRequestInterceptorHolder.getInterceptorList();
            for (RpcInvokeInterceptor<RpcRequest,RpcResponse> interceptor : interceptorList){
                try {
                    interceptor.postIntercept(providerName, response);
                }catch (Exception e){
                    logger.error("RpcStarter::Request: postIntercept[{}] error:",interceptor.getClass(),e.getMessage(),e);
                }
            }
        }
    }
}
