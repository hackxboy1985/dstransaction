package cn.itweknow.sbrpccorestarter.consumer;


import cn.itweknow.sbrpccorestarter.common.RpcDecoder;
import cn.itweknow.sbrpccorestarter.common.RpcEncoder;
import cn.itweknow.sbrpccorestarter.model.ProviderInfo;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @author 链接不保持版，请求完后即断开，但其中send方法调用的RpcClientPool可对链接保持，无须重复连接。
 * @date 2020/12/26 18:09
 * @description
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private String host;

    private int port;

    private CompletableFuture<String> future;

    /**
     * 用来接收服务器端的返回的。
     */
    private RpcResponse response;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RpcResponse send(ProviderInfo providerInfo, RpcRequest request, boolean keepalive){
        if (keepalive){
            return send(providerInfo, request);
        }
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(RpcClient.this);
                        }
                    });
            // 连接服务器
            logger.info("RpcStarter::client connecting provider {}:{}", host,port);
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            logger.info("RpcStarter::client send request {}", request);
            channelFuture.channel().writeAndFlush(request).sync();
            future = new CompletableFuture<>();
            future.get();
            if (response != null) {
                // 关闭netty连接。
//                channelFuture.channel().closeFuture().sync();//同步不适合
//                channelFuture.channel().close();
                channelFuture.addListener(ChannelFutureListener.CLOSE);
            }
            return response;
        } catch (Exception e) {
            logger.error("RpcStarter::client send msg error,", e);
            return null;
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public RpcResponse send(ProviderInfo providerInfo, RpcRequest request) {
        return RpcClientPool.getRpcClientPool().send(providerInfo,request);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("RpcStarter::client connected provider {}:{}", host,port);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("RpcStarter::client，disconnect provider {}:{}", host,port);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RpcResponse rpcResponse) throws Exception {
        logger.info("RpcStarter::client get request result,{}", rpcResponse);
        this.response = rpcResponse;
        future.complete("");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("RpcStarter::netty client caught exception,", cause);
        ctx.close();
    }
}
