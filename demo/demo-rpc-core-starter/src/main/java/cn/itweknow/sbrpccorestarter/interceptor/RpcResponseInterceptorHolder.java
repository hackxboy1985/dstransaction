package cn.itweknow.sbrpccorestarter.interceptor;


import java.util.ArrayList;
import java.util.List;

public class RpcResponseInterceptorHolder {

    List<RpcInvokeInterceptor> requestInterceptors;

    RpcResponseInterceptorHolder(List<RpcInvokeInterceptor> list){
        this.requestInterceptors = list;
    }

    public void add(RpcInvokeInterceptor interceptor){
        requestInterceptors.add(interceptor);
    }

    public List<RpcInvokeInterceptor> getInterceptorList(){return requestInterceptors;}

    public static RpcResponseInterceptorHolder builder() {
        return new RpcResponseInterceptorHolder(new ArrayList<>());
    }

}
