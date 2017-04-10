package com.simple.rpc.nio.server;

import com.simple.rpc.nio.client.AbstractRpcConnector;

/**
 * Created by stephen.zhang on 17/4/10.
 */
public interface RpcOutputNofity {
    public void notifySend(AbstractRpcConnector connector);
}
