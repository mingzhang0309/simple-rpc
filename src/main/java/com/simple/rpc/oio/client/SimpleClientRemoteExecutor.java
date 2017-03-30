package com.simple.rpc.oio.client;

import com.simple.rpc.common.*;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.common.serializer.JdkSerializer;
import com.simple.rpc.common.serializer.RpcSerializer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public class SimpleClientRemoteExecutor implements Service,RemoteExecutor,RpcCallListener {
    private RpcOioConnector connector;
    private AtomicInteger index = new AtomicInteger(10000);
    protected int timeout = 10000;
    private RpcSerializer serializer;
    private RpcSync clientRpcSync;
    private ConcurrentHashMap<String,RpcCallSync> rpcCache = new ConcurrentHashMap<String,RpcCallSync>();

    public SimpleClientRemoteExecutor(RpcOioConnector connector) {
        serializer = new JdkSerializer();
        this.connector = connector;
        clientRpcSync = new SimpleFutureRpcSync();
        connector.addRpcCallListener(this);
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
        RpcObject request = new RpcObject(INVOKE, this.genIndex(), length, buffer);
        RpcCallSync sync = new RpcCallSync(request.getIndex(), request);
        rpcCache.put(this.genRpcCallCacheKey(request.getThreadId(), request.getIndex()), sync);
        rpcOioConnector.sendRpcObject(request, timeout);
        clientRpcSync.waitForResult(timeout, sync);
        RpcObject response = sync.getResponse();
        if (response == null) {
            throw new RpcException("null rpc response");
        }
        if (response.getType() == RpcUtils.RpcType.FAIL) {
            String message = "remote rpc call failed";
            if (response.getLength() > 0) {
                message = new String(response.getData());
            }
            throw new RpcException(message);
        }
        if (response.getLength() > 0) {
            return serializer.deserialize(sync.getResponse().getData());
        }
        return null;
    }

    private String genRpcCallCacheKey(long threadId,int index){
        return "rpc_"+threadId+"_"+index;
    }

    private int genIndex(){
        return index.getAndIncrement();
    }

    public RpcOioConnector getRpcConnector(RemoteCall call) {
        return connector;
    }

    @Override
    public void onRpcMessage(RpcObject rpc, RpcSender sender) {
        RpcCallSync sync = rpcCache.get(this.genRpcCallCacheKey(rpc.getThreadId(), rpc.getIndex()));
        if(sync != null && sync.getRequest().getThreadId() == rpc.getThreadId()) {
            clientRpcSync.notifyResult(sync, rpc);
        }
    }
}
