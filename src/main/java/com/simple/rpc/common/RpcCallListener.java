package com.simple.rpc.common;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public interface RpcCallListener {
    public void onRpcMessage(RpcObject rpc,RpcSender sender);
}
