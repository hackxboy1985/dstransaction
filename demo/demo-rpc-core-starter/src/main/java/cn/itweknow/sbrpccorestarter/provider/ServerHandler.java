package cn.itweknow.sbrpccorestarter.provider;

import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sj
 * @date 2020/12/29 19:12
 * @description
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory
            .getLogger(ServerHandler.class);

    RpcProviderHandler rpcProviderHandler;

    public ServerHandler(){}
    public ServerHandler(RpcProviderHandler rpcProviderHandler){
        this.rpcProviderHandler=rpcProviderHandler;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("RpcStarter::Provider::server accept connect {},{}",  ctx.channel().remoteAddress().toString(),ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("RpcStarter::Provide::server disconnected {},{}",  ctx.channel().remoteAddress().toString(), ctx.channel().id());
        //ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RpcRequest request) throws Exception {
        logger.info("RpcStarter::Provider::server receive data request,{}", request);
        RpcResponse rpcResponse = rpcProviderHandler.handle(request);

        //TODO:addListener(ChannelFutureListener.CLOSE)会异步断开连接,服务端不应该主动断开连接.
//        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
        channelHandlerContext.writeAndFlush(rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("RpcStarter::Provider::server netty caught error {},", ctx.channel().remoteAddress().toString(),cause);
        ctx.close();
    }


}
