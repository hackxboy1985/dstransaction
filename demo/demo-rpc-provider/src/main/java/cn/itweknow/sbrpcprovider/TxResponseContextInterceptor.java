package cn.itweknow.sbrpcprovider;

import cn.ds.transaction.framework.context.SagaContext;
import cn.itweknow.sbrpccorestarter.interceptor.RpcInvokeInterceptor;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cn.ds.transaction.framework.context.SagaContext.GLOBAL_TX_ID_KEY;
import static cn.ds.transaction.framework.context.SagaContext.LOCAL_TX_ID_KEY;

@Component
public class TxResponseContextInterceptor implements RpcInvokeInterceptor<RpcRequest,RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required=false)
    private SagaContext sagaContext;

    @Override
    public RpcResponse preIntercept(String providerName, RpcRequest request) {

        if (sagaContext != null) {
            String globalTxId = (String)request.getContext().get(GLOBAL_TX_ID_KEY);
            if (globalTxId == null) {
                logger.info("RpcInvokeInterceptor::Cannot inject transaction ID, no such saga context global id: {}", GLOBAL_TX_ID_KEY);
            } else {
                sagaContext.setGlobalTxId(globalTxId);
                sagaContext.setLocalTxId((String)request.getContext().get(LOCAL_TX_ID_KEY));
                logger.info("RpcInvokeInterceptor::Added {} {} and {} {} to sagaContext", new Object[] {GLOBAL_TX_ID_KEY, sagaContext.globalTxId(),
                        LOCAL_TX_ID_KEY, sagaContext.localTxId()});
            }
        } else {
            logger.debug("RpcInvokeInterceptor::Cannot inject transaction ID, as the SagaContext is null.");
        }

        return null;
    }

    @Override
    public RpcResponse postIntercept(String providerName, RpcResponse response) {
        logger.info("RpcInvokeInterceptor::Response postIntercept: context:{}",response);
        return null;
    }

}
