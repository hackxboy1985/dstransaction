package cn.itweknow.sbrpccorestarter.provider;

import cn.itweknow.sbrpccorestarter.anno.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

public class InstantiationTracingBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        init();
    }

    void  init() {
        logger.info("RpcStarter::Provider::ScanServer [start] rpc server scan provider service...");
        Map<String, Object> beanMap = this.applicationContext.getBeansWithAnnotation(RpcService.class);
        if (null != beanMap && !beanMap.isEmpty()) {
            beanMap.entrySet().forEach(one -> {
                initProviderBean(one.getKey(), one.getValue());
            });
        }
        logger.info("RpcStarter::Provider::ScanServer [end] rpc server scan provider service...");
        // 如果有服务的话才启动netty server
//        if (!beanMap.isEmpty()) {
//            startNetty(rpcProperties.getPort());
//        }
    }

    /**
     * 将服务类交由BeanFactory管理
     * @param beanName
     * @param bean
     */
    private void initProviderBean(String beanName, Object bean) {
        logger.info("RpcStarter::Provider::ScanServer find provider #{}# init.",beanName);

        RpcService rpcService = this.applicationContext
                .findAnnotationOnBean(beanName, RpcService.class);
        BeanFactory.addBean(rpcService.value(), bean);
    }
}
