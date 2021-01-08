package cn.itweknow.sbrpccorestarter.provider;

import cn.itweknow.sbrpccorestarter.interceptor.RpcResponseInterceptorProcessor;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author sj
 * @date 2020/12/29 19:12
 * @description
 */
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory
            .getLogger(ServerHandler.class);

//    RpcResponseInterceptorProcessor rpcResponseInterceptorProcessor;
    RpcProviderHandler rpcProviderHandler;

    public ServerHandler(){}
//    public ServerHandler(RpcResponseInterceptorProcessor rpcResponseInterceptorProcessor){
//        this.rpcResponseInterceptorProcessor=rpcResponseInterceptorProcessor;
//    }
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
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RpcRequest request) throws Exception {
        logger.info("RpcStarter::Provider::server receive data request,{}", request);
        RpcResponse rpcResponse = rpcProviderHandler.handle(request);
//        // 返回的对象。
//        RpcResponse rpcResponse = new RpcResponse();
//        // 将请求id原路带回
//        rpcResponse.setRequestId(request.getRequestId());
//        try {
//            if (rpcResponseInterceptorProcessor != null)
//            rpcResponseInterceptorProcessor.preIntercept(request.getClassName(),request);
//            Object result = handle(request);
//            rpcResponse.setResult(result);
//            if (rpcResponseInterceptorProcessor != null)
//            rpcResponseInterceptorProcessor.postIntercept(request.getClassName(),request);
//        } catch (Exception e) {
//            rpcResponse.setError(e);
//            rpcResponse.setMsg(e.getMessage());
//        }

        //TODO:addListener(ChannelFutureListener.CLOSE)会异步断开连接,服务端不应该主动断开连接.
//        channelHandlerContext.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
        channelHandlerContext.writeAndFlush(rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("RpcStarter::Provider::server netty caught error {},", ctx.channel().remoteAddress().toString(),cause);
        ctx.close();
    }


    //TODO:拆出个具体实现类来实现对响应的拦截处理
//    private Object handle(RpcRequest request) throws Exception {
//        String className = request.getClassName();
//        Class<?> objClz = Class.forName(className);
//        Object o = BeanFactory.getBean(objClz);
//        // 获取调用的方法名称。
//        String methodName = request.getMethodName();
//        // 参数类型
//        Class<?>[] paramsTypes = request.getParamTypes();
//        // 具体参数。
//        Object[] params = request.getParams();
//        // 调用实现类的指定的方法并返回结果。
//        logger.info("RpcStarter::Provider::invoke classname={}, methodName={}",className,methodName);
//        Method method = objClz.getMethod(methodName, paramsTypes);
//        Object res = method.invoke(o, params);
//        return res;
//    }
}
