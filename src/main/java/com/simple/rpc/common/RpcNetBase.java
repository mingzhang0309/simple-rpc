package com.simple.rpc.common;

import com.simple.rpc.nio.client.AbstractRpcConnector;
import com.simple.rpc.oio.client.RpcOioConnector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public abstract class RpcNetBase extends AbstractRpcNetworkBase implements RpcNetExceptionHandler {
    protected List<RpcCallListener> callListeners;

    public RpcNetBase() {
        callListeners = new LinkedList<RpcCallListener>();
    }

    public List<RpcCallListener> getCallListeners() {
        return callListeners;
    }

    public void setCallListeners(List<RpcCallListener> callListeners) {
        this.callListeners = callListeners;
    }

    public void addRpcCallListener(RpcCallListener listener) {
        callListeners.add(listener);
    }

    public void fireCallListeners(RpcObject rpc, RpcSender sender) {
        for (RpcCallListener listener : callListeners) {
            listener.onRpcMessage(rpc, sender);
        }
    }

    public void startListeners() {
        for (RpcCallListener listener : callListeners) {
            if (listener instanceof Service) {
                Service service = (Service) listener;
                service.startService();
            }
        }
    }

    public void stopListeners() {
        for (RpcCallListener listener : callListeners) {
            if (listener instanceof Service) {
                Service service = (Service) listener;
                service.stopService();
            }
        }
    }

    public void addConnectorListeners(AbstractRpcConnector connector) {
        for (RpcCallListener listener : callListeners) {
            connector.addRpcCallListener(listener);
        }
    }
}
