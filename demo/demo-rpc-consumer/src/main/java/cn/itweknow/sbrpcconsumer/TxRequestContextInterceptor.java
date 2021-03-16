package cn.itweknow.sbrpcconsumer;

import cn.ds.transaction.framework.context.SagaContext;
import cn.itweknow.sbrpccorestarter.interceptor.RpcInvokeInterceptor;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static cn.ds.transaction.framework.context.SagaContext.GLOBAL_TX_ID_KEY;
import static cn.ds.transaction.framework.context.SagaContext.LOCAL_TX_ID_KEY;

@Component
public class TxRequestContextInterceptor implements RpcInvokeInterceptor<RpcRequest,RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required=false)
    private SagaContext sagaContext;

    @Override
    public RpcResponse preIntercept(String providerName, RpcRequest request) {
        if(request.getContext()==null){
            request.setContext(new HashMap());
        }
        if (sagaContext != null) {
            //request.getContext().put("testkey","value");
            request.getContext().put(GLOBAL_TX_ID_KEY, sagaContext.globalTxId());
            request.getContext().put(LOCAL_TX_ID_KEY, sagaContext.localTxId());
        }
        return null;
    }

    @Override
    public RpcResponse postIntercept(String providerName, RpcResponse response) {
//        logger.info("Request PostIntercept: context:{}",request.getContext());
        return null;
    }
}
