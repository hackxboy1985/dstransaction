package cn.itweknow.sbrpccorestarter.interceptor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;


/**
 * RpcRequestInterceptor的收集器
 */
public class RpcRequestInterceptorRegister implements BeanPostProcessor {

    RpcRequestInterceptorHolder rpcRequestInterceptorHolder;

    public RpcRequestInterceptorRegister(RpcRequestInterceptorHolder requestInterceptorHolder){
        this.rpcRequestInterceptorHolder = requestInterceptorHolder;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        checkRequestInterceptor(bean);
        return bean;
    }

    void checkRequestInterceptor(Object bean) {
        if (RpcInvokeInterceptor.class.isAssignableFrom(bean.getClass())) {
            rpcRequestInterceptorHolder.add((RpcInvokeInterceptor)bean);
        }
    }
}
