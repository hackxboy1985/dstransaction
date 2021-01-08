package cn.itweknow.sbrpccorestarter.interceptor;

import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * RpcResponseInterceptor的响应处理器
 */
public class RpcResponseInterceptorProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());


    RpcResponseInterceptorHolder rpcResponseInterceptorHolder;

    public RpcResponseInterceptorProcessor(RpcResponseInterceptorHolder rpcResponseInterceptorHolder){
        this.rpcResponseInterceptorHolder = rpcResponseInterceptorHolder;
    }

    public void preIntercept(String providerName, RpcRequest request){
        if (CollectionUtils.isEmpty(rpcResponseInterceptorHolder.getInterceptorList()) == false){
            List<RpcInvokeInterceptor> interceptorList = rpcResponseInterceptorHolder.getInterceptorList();
            for (RpcInvokeInterceptor<RpcRequest,RpcResponse> interceptor : interceptorList){
                try {
                    interceptor.preIntercept(providerName, request);
                }catch (Exception e){
                    logger.error("RpcStarter::Provider: preIntercept[{}] error:",interceptor.getClass(),e.getMessage(),e);
                }
            }
        }
    }

    public void postIntercept(String providerName, RpcRequest request,RpcResponse rpcResponse){
        if (CollectionUtils.isEmpty(rpcResponseInterceptorHolder.getInterceptorList()) == false){
            List<RpcInvokeInterceptor> interceptorList = rpcResponseInterceptorHolder.getInterceptorList();
            for (RpcInvokeInterceptor<RpcRequest,RpcResponse> interceptor : interceptorList){
                try {
                    interceptor.postIntercept(providerName, rpcResponse);
                }catch (Exception e){
                    logger.error("RpcStarter::Provider: postIntercept[{}] error:",interceptor.getClass(),e.getMessage(),e);
                }
            }
        }
    }
}
