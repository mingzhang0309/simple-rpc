package com.simple.rpc.oio.server;

import com.simple.rpc.common.RemoteCall;
import com.simple.rpc.common.RemoteExecutor;
import com.simple.rpc.common.RpcServiceBean;
import com.simple.rpc.common.RpcUtils;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.common.exception.RpcExceptionHandler;
import com.simple.rpc.common.exception.SimpleRpcExceptionHandler;
import com.simple.rpc.common.server.RpcServicesHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class SimpleServerRemoteExecutor implements RemoteExecutor, RpcServicesHolder {
    protected ConcurrentHashMap<String,RpcServiceBean> exeCache = new ConcurrentHashMap<String,RpcServiceBean>();

    /**
     * 当前应用
     */
    private String application;

    /**
     * 业务方法执行异常处理器
     */
    private RpcExceptionHandler exceptionHandler;

    public SimpleServerRemoteExecutor(){
        exceptionHandler = new SimpleRpcExceptionHandler();
    }

    @Override
    public void oneway(RemoteCall call) {
        RpcUtils.invokeMethod(this.findService(call), call.getMethod(), call.getArgs(), exceptionHandler);
    }

    @Override
    public Object invoke(RemoteCall call) {
        return RpcUtils.invokeMethod(this.findService(call), call.getMethod(), call.getArgs(), exceptionHandler);
    }

    @Override
    public List<RpcServiceBean> getRpcServices() {
        ArrayList<RpcServiceBean> list = new ArrayList<RpcServiceBean>();
        list.addAll(exeCache.values());
        return list;
    }

    @Override
    public void startService() {

    }

    @Override
    public void stopService() {

    }

    /**
     * 注册remote服务
     * @param clazz
     * @param ifaceImpl
     */
    public void registerRemote(Class<?> clazz, Object ifaceImpl) {
        this.registerRemote(clazz, ifaceImpl, null, null);
    }

    /**
     * 注册remote服务
     * @param clazz
     * @param ifaceImpl
     * @param version
     */
    public void registerRemote(Class<?> clazz,Object ifaceImpl,String version,String group){
        //validate impl java object
        Object service = exeCache.get(clazz.getName());
        if(service!=null&&service!=ifaceImpl){
            throw new RpcException("can't register service "+clazz.getName()+" again");
        }
        if(ifaceImpl==service||ifaceImpl==null){
            return;
        }
        if(version==null){
            version=RpcUtils.DEFAULT_VERSION;
        }

        //默认分组
        if(group==null){
            group = RpcUtils.DEFAULT_GROUP;
        }
        //添加类型映射
//        XAliasUtils.addServiceRefType(clazz);

        exeCache.put(this.genExeKey(clazz.getName(), version, group), new RpcServiceBean(clazz, ifaceImpl, version,
                application, group));
    }

    private String genExeKey(String service,String version,String group){
        if(version!=null){
            return group+"_"+service+"_"+version;
        }
        return service;
    }

    private Object findService(RemoteCall call){
        String exeKey = this.genExeKey(call.getService(), call.getVersion(),call.getGroup());
        RpcServiceBean object = exeCache.get(exeKey);
        if(object==null||object.getBean()==null){
            throw new RpcException("group:"+call.getGroup()+" service:"+call.getService()+" version:"+call.getVersion()+" not exist");
        }
        return object.getBean();
    }
}
