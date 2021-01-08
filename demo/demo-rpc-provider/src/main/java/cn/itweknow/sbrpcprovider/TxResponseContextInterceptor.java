package cn.itweknow.sbrpcprovider;

import cn.ds.transaction.framework.context.OmegaContext;
import cn.itweknow.sbrpccorestarter.interceptor.RpcInvokeInterceptor;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static cn.ds.transaction.framework.context.OmegaContext.GLOBAL_TX_ID_KEY;
import static cn.ds.transaction.framework.context.OmegaContext.LOCAL_TX_ID_KEY;

@Component
public class TxResponseContextInterceptor implements RpcInvokeInterceptor<RpcRequest,RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required=false)
    private OmegaContext omegaContext;

    @Override
    public RpcResponse preIntercept(String providerName, RpcRequest request) {

        if (omegaContext != null) {
            String globalTxId = (String)request.getContext().get(GLOBAL_TX_ID_KEY);
            if (globalTxId == null) {
                logger.info("Cannot inject transaction ID, no such omega context global id: {}", GLOBAL_TX_ID_KEY);
            } else {
                omegaContext.setGlobalTxId(globalTxId);
                omegaContext.setLocalTxId((String)request.getContext().get(LOCAL_TX_ID_KEY));
                logger.info("Added {} {} and {} {} to omegaContext", new Object[] {GLOBAL_TX_ID_KEY, omegaContext.globalTxId(),
                        LOCAL_TX_ID_KEY, omegaContext.localTxId()});
            }
        } else {
            logger.debug("Cannot inject transaction ID, as the OmegaContext is null.");
        }

        return null;
    }

    @Override
    public RpcResponse postIntercept(String providerName, RpcRequest request) {
        logger.info("Response postIntercept: context:{}",request.getContext());
        return null;
    }
}
