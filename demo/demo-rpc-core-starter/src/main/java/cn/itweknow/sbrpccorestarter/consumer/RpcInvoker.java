package cn.itweknow.sbrpccorestarter.consumer;

import cn.itweknow.sbrpccorestarter.interceptor.RpcRequestInterceptorProcessor;
import cn.itweknow.sbrpccorestarter.model.ProviderInfo;
import cn.itweknow.sbrpccorestarter.model.RpcRequest;
import cn.itweknow.sbrpccorestarter.model.RpcResponse;
import cn.itweknow.sbrpccorestarter.registory.ServiceDiscovery;

public class RpcInvoker {

    private ServiceDiscovery serviceDiscovery;

    private RpcRequestInterceptorProcessor rpcRequestInterceptorProcessor;

    public RpcResponse invoke(String providerName, RpcRequest request){
        // 获取一个服务提供者。
        ProviderInfo providerInfo = serviceDiscovery.discover(providerName);
        if (providerInfo == null){
            throw new RuntimeException("RpcStarter::RpcInvoker 找不到"+request.getClassName()+"::"+request.getMethodName()+"的服务提供者:"+providerName);
        }

        rpcRequestInterceptorProcessor.preIntercept(providerName,request);

        // 解析服务提供者的地址信息，数组第一个元素为ip地址，第二个元素为端口号。
        String[] addrInfo = providerInfo.getAddr().split(":");
        String host = addrInfo[0];
        int port = Integer.parseInt(addrInfo[1]);
        RpcClient rpcClient = new RpcClient(host, port);
        // 发送调用消息。
        RpcResponse response = rpcClient.send(providerInfo,request,true);

        rpcRequestInterceptorProcessor.postIntercept(providerName,request);
        return response;
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery){
        this.serviceDiscovery = serviceDiscovery;
    }
    public void setRpcRequestInterceptorProcessor(RpcRequestInterceptorProcessor requestInterceptorProcessor){this.rpcRequestInterceptorProcessor = requestInterceptorProcessor;}
}
