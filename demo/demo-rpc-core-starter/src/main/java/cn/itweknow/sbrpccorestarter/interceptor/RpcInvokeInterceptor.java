package cn.itweknow.sbrpccorestarter.interceptor;

public interface RpcInvokeInterceptor<T,V> {
    V preIntercept(String providerName, T request);
    V postIntercept(String providerName, T request);
}
