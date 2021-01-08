package cn.itweknow.sbrpccorestarter.interceptor;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RpcRequestInterceptorHolder {

    List<RpcInvokeInterceptor> requestInterceptors;

    RpcRequestInterceptorHolder(List<RpcInvokeInterceptor> list){
        this.requestInterceptors = list;
    }

    public void add(RpcInvokeInterceptor interceptor){
        requestInterceptors.add(interceptor);
    }

    public List<RpcInvokeInterceptor> getInterceptorList(){return Collections.unmodifiableList(requestInterceptors);}

    public static RpcRequestInterceptorHolder builder() {
        return new RpcRequestInterceptorHolder(new ArrayList<>());
    }

}
