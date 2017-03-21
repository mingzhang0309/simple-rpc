package com.simple.rpc.common.exception;

import com.simple.rpc.common.RemoteCall;
import com.simple.rpc.common.RpcObject;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public interface RpcExceptionHandler {
    public void handleException(RpcObject rpc, RemoteCall call, Throwable e);
}
