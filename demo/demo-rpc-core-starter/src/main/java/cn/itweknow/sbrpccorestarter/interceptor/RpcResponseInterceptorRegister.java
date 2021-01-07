package cn.itweknow.sbrpccorestarter.interceptor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * RpcInvokeInterceptor的收集器
 */
public class RpcResponseInterceptorRegister implements BeanPostProcessor {

    RpcResponseInterceptorHolder rpcResponseInterceptorHolder;

    public RpcResponseInterceptorRegister(RpcResponseInterceptorHolder responseInterceptorHolder){
        this.rpcResponseInterceptorHolder = responseInterceptorHolder;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        checkResponseInterceptor(bean);
        return bean;
    }

    void checkResponseInterceptor(Object bean) {
        if (RpcInvokeInterceptor.class.isAssignableFrom(bean.getClass())) {
            rpcResponseInterceptorHolder.add((RpcInvokeInterceptor)bean);
        }
    }
}
