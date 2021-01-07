package cn.itweknow.sbrpccorestarter.model;

import java.util.Arrays;
import java.util.Map;

/**
 * @author sj
 * @date 2020/12/26 17:21
 * @description
 */
public class RpcRequest {


    private String requestId;

    private String className;

    private String methodName;

    private Map context;

    private Class<?>[] paramTypes;

    private Object[] params;

    public String getRequestId() {
        return requestId;
    }

    public RpcRequest setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public RpcRequest setClassName(String className) {
        this.className = className;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public RpcRequest setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public RpcRequest setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
        return this;
    }

    public Object[] getParams() {
        return params;
    }

    public RpcRequest setParams(Object[] params) {
        this.params = params;
        return this;
    }

    public Map getContext() {
        return context;
    }

    public void setContext(Map context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestId='" + requestId + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", context='" + context + '\'' +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
