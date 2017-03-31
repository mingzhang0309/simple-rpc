package com.simple.rpc.oio.client;

import com.simple.rpc.common.RemoteCall;
import com.simple.rpc.common.RemoteExecutor;
import com.simple.rpc.common.RpcUtils;
import com.simple.rpc.common.Service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public class SimpleClientRemoteProxy implements InvocationHandler, Service {
    private ConcurrentHashMap<Class,String> versionCache = new ConcurrentHashMap<Class,String>();
    private ConcurrentHashMap<Class,String> groupCache = new ConcurrentHashMap<Class,String>();
    private RemoteExecutor remoteExecutor;

    public SimpleClientRemoteProxy() {
    }

    public SimpleClientRemoteProxy(RemoteExecutor remoteExecutor) {
        this.remoteExecutor = remoteExecutor;
    }

    public RemoteExecutor getRemoteExecutor() {
        return remoteExecutor;
    }

    public void setRemoteExecutor(RemoteExecutor remoteExecutor) {
        this.remoteExecutor = remoteExecutor;
    }

    public <T> T registerRemote(Class<T> remote) {
        return this.registerRemote(remote, RpcUtils.DEFAULT_VERSION);
    }

    private <T> T registerRemote(Class<T> remote, String defaultVersion) {
        return this.registerRemote(remote, defaultVersion, RpcUtils.DEFAULT_GROUP);
    }

    private <T> T registerRemote(Class<T> remote, String version, String group) {
        T result = (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{remote}, this);
        if(version==null){
            version = RpcUtils.DEFAULT_VERSION;
        }

        versionCache.put(remote, version);

        if(group==null){
            group = RpcUtils.DEFAULT_GROUP;
        }

        groupCache.put(remote,group);
        return result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> service = method.getDeclaringClass();
        String name = method.getName();
        RemoteCall call = new RemoteCall(service.getName(), name);
        call.setArgs(args);
        String version = versionCache.get(service);
        if (version != null) {
            call.setVersion(version);
        } else {
            call.setVersion(RpcUtils.DEFAULT_VERSION);
        }

        String group = groupCache.get(service);
        if (group == null) {
            group = RpcUtils.DEFAULT_GROUP;
        }
        call.setGroup(group);

        if (method.getReturnType() == void.class) {
            remoteExecutor.oneway(call);
            return null;
        }
        return remoteExecutor.invoke(call);
    }

    @Override
    public void startService() {
        remoteExecutor.startService();
    }

    @Override
    public void stopService() {
        remoteExecutor.stopService();
    }
}
