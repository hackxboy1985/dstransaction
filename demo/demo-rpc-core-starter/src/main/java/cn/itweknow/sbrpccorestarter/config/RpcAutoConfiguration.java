package cn.itweknow.sbrpccorestarter.config;

import cn.itweknow.sbrpccorestarter.consumer.RpcInvoker;
import cn.itweknow.sbrpccorestarter.consumer.RpcProxy;
import cn.itweknow.sbrpccorestarter.exception.ZkConnectException;
import cn.itweknow.sbrpccorestarter.registory.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author sj
 * @date 2020/12/30 11:06
 * @description
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class RpcAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RpcAutoConfiguration.class);

    @Autowired
    private RpcProperties rpcProperties;

    @Bean
    @ConditionalOnMissingBean
    public ServiceDiscovery serviceDiscovery() {
        ServiceDiscovery serviceDiscovery =
                null;
        try {
            serviceDiscovery = new ServiceDiscovery(rpcProperties.getRegisterAddress());
        } catch (ZkConnectException e) {
            logger.error("zk connect failed:", e);
        }
        return serviceDiscovery;
    }



}
