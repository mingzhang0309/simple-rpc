package com.simple.rpc.oio.client;

import com.simple.rpc.common.RemoteCall;
import com.simple.rpc.common.RemoteExecutor;
import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.Service;
import com.simple.rpc.common.serializer.JdkSerializer;
import com.simple.rpc.common.serializer.RpcSerializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public class SimpleClientRemoteExecutor implements Service,RemoteExecutor {
    private RpcOioConnector connector;
    private AtomicInteger index = new AtomicInteger(10000);
    protected int timeout = 10000;
    private RpcSerializer serializer;

    public SimpleClientRemoteExecutor(RpcOioConnector connector) {
        serializer = new JdkSerializer();
        this.connector = connector;
    }

    public RpcOioConnector getConnector() {
        return connector;
    }

    public void setConnector(RpcOioConnector connector) {
        this.connector = connector;
    }

    @Override
    public void startService() {
        connector.startService();
    }

    @Override
    public void stopService() {
        connector.stopService();
    }

    @Override
    public void oneway(RemoteCall call) {
        RpcOioConnector connector = getRpcConnector(call);
        byte[] buffer = serializer.serialize(call);
        int length = buffer.length;
        RpcObject rpc = new RpcObject(ONEWAY, this.genIndex(), length, buffer);
        connector.sendRpcObject(rpc, timeout);
    }

    @Override
    public Object invoke(RemoteCall call) {
        RpcOioConnector rpcOioConnector = getRpcConnector(call);
        byte[] buffer = serializer.serialize(call);
        int length = buffer.length;
        RpcObject request = new RpcObject(INVOKE,this.genIndex(),length,buffer);
        rpcOioConnector.sendRpcObject(request, timeout);
        return null;
    }

    private int genIndex(){
        return index.getAndIncrement();
    }

    public RpcOioConnector getRpcConnector(RemoteCall call) {
        return connector;
    }
}
