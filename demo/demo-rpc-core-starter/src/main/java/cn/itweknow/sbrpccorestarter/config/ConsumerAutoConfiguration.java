package cn.itweknow.sbrpccorestarter.config;

import cn.itweknow.sbrpccorestarter.anno.RpcConsumer;
import cn.itweknow.sbrpccorestarter.consumer.RpcInvoker;
import cn.itweknow.sbrpccorestarter.consumer.RpcProxy;
import cn.itweknow.sbrpccorestarter.interceptor.RpcRequestInterceptorHolder;
import cn.itweknow.sbrpccorestarter.interceptor.RpcRequestInterceptorProcessor;
import cn.itweknow.sbrpccorestarter.interceptor.RpcRequestInterceptorRegister;
import cn.itweknow.sbrpccorestarter.registory.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.lang.reflect.Field;

/**
 * @author sj
 * @date 2020/12/29 19:53
 * @description
 */
@Configuration
//@ConditionalOnClass(RpcConsumer.class)
@ConditionalOnProperty(value = {"spring.rpc.server-name"}, matchIfMissing = true)
//@ConditionalOnExpression("'${spring.rpc.server-name}' == null && '${spring.rpc.server-name}'.length() == 0 ? true : false ")
//@ConditionalOnExpression("'${spring.rpc.server-name} != null ? false : true '")//没有rpc服务名时，才认为是consumer端，才初始化
@EnableConfigurationProperties(RpcProperties.class)
public class ConsumerAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public RpcProxy rpcProxy() {
        RpcProxy rpcProxy = new RpcProxy();
        return rpcProxy;
    }

    @Bean
    @ConditionalOnMissingBean
    public RpcInvoker rpcInvoker(ServiceDiscovery serviceDiscovery,RpcRequestInterceptorProcessor requestInterceptorProcessor) {
        RpcInvoker rpcInvoker = new RpcInvoker();
        rpcInvoker.setServiceDiscovery(serviceDiscovery);
        rpcInvoker.setRpcRequestInterceptorProcessor(requestInterceptorProcessor);
        return rpcInvoker;
    }


    /**
     * 设置动态代理
     * @return
     */
    @Bean
    public BeanPostProcessor beanPostProcessor(RpcProxy rpcProxy) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName)
                    throws BeansException {
                Class<?> objClz = bean.getClass();
                for (Field field : objClz.getDeclaredFields()) {
                    RpcConsumer rpcConsumer = field.getAnnotation(RpcConsumer.class);
                    if (null != rpcConsumer) {
                        Class<?> type = field.getType();
                        field.setAccessible(true);
                        try {
                            //System.out.println("set rpc field to proxy object!");
                            field.set(bean, rpcProxy.create(type, rpcConsumer.providerName()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } finally {
                            field.setAccessible(false);
                        }
                    }
                }
                return bean;
            }
        };
    }

    @Bean
    public RpcRequestInterceptorHolder rpcRequestInterceptorHolder(){
        return RpcRequestInterceptorHolder.builder();
    }

    @Bean
    public RpcRequestInterceptorProcessor rpcRequestInterceptorProcessor(RpcRequestInterceptorHolder requestInterceptorHolder){
        return new RpcRequestInterceptorProcessor(requestInterceptorHolder);
    }

    @Bean
    public RpcRequestInterceptorRegister rpcRequestInterceptorRegister(RpcRequestInterceptorHolder requestInterceptorHolder){
        return new RpcRequestInterceptorRegister(requestInterceptorHolder);
    }


}
