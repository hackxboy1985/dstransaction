package cn.itweknow.sbrpccorestarter.config;

import cn.itweknow.sbrpccorestarter.anno.RpcService;
import cn.itweknow.sbrpccorestarter.common.RpcDecoder;
import cn.itweknow.sbrpccorestarter.common.RpcEncoder;
import cn.itweknow.sbrpccorestarter.exception.ZkConnectException;
import cn.itweknow.sbrpccorestarter.interceptor.RpcResponseInterceptorHolder;
import cn.itweknow.sbrpccorestarter.interceptor.RpcResponseInterceptorProcessor;
import cn.itweknow.sbrpccorestarter.interceptor.RpcResponseInterceptorRegister;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import cn.itweknow.sbrpccorestarter.provider.BeanFactory;
import cn.itweknow.sbrpccorestarter.provider.InstantiationTracingBeanPostProcessor;
import cn.itweknow.sbrpccorestarter.provider.RpcProviderHandler;
import cn.itweknow.sbrpccorestarter.provider.ServerHandler;
import cn.itweknow.sbrpccorestarter.registory.RegistryServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

//import io.netty.handler.codec.memcache.binary.BinaryMemcacheObjectAggregator;
//import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author sj
 * @date 2020/12/26 14:07
 * @description
 */
@Configuration
//@ConditionalOnClass(RpcService.class)
@ConditionalOnBean(annotation=RpcService.class)//服务提供者(provider)才有包含RpcService注解的类
public class ProviderAutoConfiguration {

    private Logger logger = LoggerFactory.getLogger(ProviderAutoConfiguration.class);

    @Autowired
    private RpcProperties rpcProperties;

    /**
     * 这里注入bean,@!!!!!!!!!!!!!!!延后调用才能不正常打断流程
     */
    @PostConstruct
    public void  postContruct() {
    }

    /**
     * 启动netty server
     * @param port
     *          netty启动的端口
     */
    public void startNetty(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //.addLast(new ChunkedWriteHandler())//以块的方式来写的处理器
                                    //.addLast(new BinaryMemcacheObjectAggregator(8192))//以二进制内存对象形式
                                    .addLast(new RpcDecoder(RpcRequest.class))//编码
                                    .addLast(new RpcEncoder(RpcResponse.class))//解码
                                    .addLast(new ServerHandler());//handler
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            logger.info("RpcStarter::Provider: RpcServer started on port : {}", port);
            // netty服务端启动成功后，向zk注册这个服务
            System.out.println("register address="+rpcProperties.getRegisterAddress());
            new RegistryServer(rpcProperties.getRegisterAddress(),
                    rpcProperties.getTimeout(),
                    rpcProperties.getServerName(),
                    rpcProperties.getHost(), port)
                    .register();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
//            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 启动netty server
     * @param port
     *          netty启动的端口
     */
    public void startNetty(int port,ServerHandler serverHandler) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    //.addLast(new ChunkedWriteHandler())//以块的方式来写的处理器
                                    //.addLast(new BinaryMemcacheObjectAggregator(8192))//以二进制内存对象形式
                                    .addLast(new RpcDecoder(RpcRequest.class))//编码
                                    .addLast(new RpcEncoder(RpcResponse.class))//解码
                                    .addLast(serverHandler);//handler
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(port).sync();
            logger.info("RpcStarter::Provider: RpcServer started on port : {}", port);
//            logger.info("RpcStarter::Provider: RpcServer started on port : {}", port);
            // netty服务端启动成功后，向zk注册这个服务
//            System.out.println(rpcProperties.getRegisterAddress());
//            new RegistryServer(rpcProperties.getRegisterAddress(),
//                    rpcProperties.getTimeout(),
//                    rpcProperties.getServerName(),
//                    rpcProperties.getHost(), port)
//                    .register();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
//            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Bean
    public InstantiationTracingBeanPostProcessor instantiationTracingBeanPostProcessor(){
        return new InstantiationTracingBeanPostProcessor();
    }

    @Bean
    public RpcProviderHandler rpcProviderHandler(RpcResponseInterceptorProcessor rpcResponseInterceptorProcessor){
        return new RpcProviderHandler(rpcResponseInterceptorProcessor);
    }

    @Bean
    public ServerHandler serverHandler(RpcProviderHandler rpcProviderHandler){
        return new ServerHandler(rpcProviderHandler);
    }

    @Bean
    public RegistryServer registryServer(ServerHandler serverHandler) throws ZkConnectException {
        Runnable startNettyRunnable = new Runnable() {
            @Override
            public void run() {
                startNetty(rpcProperties.getPort(),serverHandler);
            }
        };
        Thread startNettyThread =new Thread(startNettyRunnable );
        startNettyThread.start();

        logger.info("RpcStarter::Provider: RpcServer register zk on port : {}", rpcProperties.getPort());
        System.out.println(rpcProperties.getRegisterAddress());
        RegistryServer registryServer = new RegistryServer(rpcProperties.getRegisterAddress(),
                rpcProperties.getTimeout(),
                rpcProperties.getServerName(),
                rpcProperties.getHost(), rpcProperties.getPort());
        registryServer.register();
        return registryServer;
    }

    @Bean
    public RpcResponseInterceptorHolder rpcResponseInterceptorHolder(){
        return RpcResponseInterceptorHolder.builder();
    }

    @Bean
    public RpcResponseInterceptorProcessor rpcResponseInterceptorProcessor(RpcResponseInterceptorHolder responseInterceptorHolder){
        return new RpcResponseInterceptorProcessor(responseInterceptorHolder);
    }

    @Bean
    public RpcResponseInterceptorRegister rpcResponseInterceptorRegister(RpcResponseInterceptorHolder responseInterceptorHolder){
        return new RpcResponseInterceptorRegister (responseInterceptorHolder);
    }

}
