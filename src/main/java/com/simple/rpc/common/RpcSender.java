package com.simple.rpc.common;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public interface RpcSender {
    public boolean sendRpcObject(RpcObject rpc,int timeout);
}
