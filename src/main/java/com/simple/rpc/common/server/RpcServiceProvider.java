package com.simple.rpc.common.server;

import com.simple.rpc.common.*;
import com.simple.rpc.common.exception.RpcExceptionHandler;
import com.simple.rpc.common.serializer.RpcSerializer;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class RpcServiceProvider implements Service, RpcCallListener {
    /**
     * 提交给上层的执行器
     */
    private RemoteExecutor executor;

    private RpcSerializer serializer;

    /**
     * 发送返回值超时时间
     */
    private int timeout = 200;

    private RpcExceptionHandler exceptionHandler;

    @Override
    public void startService() {

    }

    @Override
    public void stopService() {

    }

    @Override
    public void onRpcMessage(RpcObject rpc, RpcSender sender) {

    }
}
