package com.simple.rpc.nio.server;

import com.simple.rpc.common.RpcNetExceptionHandler;
import com.simple.rpc.common.Service;
import com.simple.rpc.nio.client.RpcNioConnector;

/**
 * Created by stephen.zhang on 17/4/10.
 */
public abstract class AbstractRpcNioSelector implements Service,RpcOutputNofity,RpcNetExceptionHandler {
    public abstract void register(RpcNioAcceptor acceptor);

    public abstract void unRegister(RpcNioAcceptor acceptor);

    public abstract void register(RpcNioConnector connector);

    public abstract void unRegister(RpcNioConnector connector);

//    public AbstractRpcNioSelector(){
//        netListeners = new LinkedList<RpcNetListener>();
//    }
//
//    protected List<RpcNetListener> netListeners;
//
//    public void addRpcNetListener(RpcNetListener listener){
//        netListeners.add(listener);
//    }
//
//    public void fireNetListeners(RpcNetBase network,Exception e){
//        for(RpcNetListener listener:netListeners){
//            listener.onClose(network,e);
//        }
//    }
}
