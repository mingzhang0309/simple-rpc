package com.simple.rpc.common.server;

import com.simple.rpc.common.*;
import com.simple.rpc.common.exception.RpcExceptionHandler;
import com.simple.rpc.common.exception.SimpleRpcExceptionHandler;
import com.simple.rpc.common.serializer.JdkSerializer;
import com.simple.rpc.common.serializer.RpcSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class RpcServiceProvider implements Service, RpcCallListener {
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceProvider.class);

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

    public RpcServiceProvider() {
        serializer = new JdkSerializer();
        exceptionHandler = new SimpleRpcExceptionHandler();
    }

    @Override
    public void startService() {

    }

    @Override
    public void stopService() {

    }

    @Override
    public void onRpcMessage(RpcObject rpc, RpcSender sender) {
        RemoteCall call = this.deserializeCall(rpc, sender);

        logger.info("RpcServiceProvider get rpc message {}", call);

        if(rpc.getType() == RpcUtils.RpcType.ONEWAY) {
            executor.oneway(call);
        }

        Object result = executor.invoke(new RemoteCall());
        rpc.setType(RpcUtils.RpcType.SUC);
        if(result != null) {
            byte[] data = serializer.serialize(result);
            rpc.setLength(data.length);
            rpc.setData(data);
        } else {
            rpc.setLength(0);
            rpc.setData(new byte[0]);
        }

        sender.sendRpcObject(rpc, timeout);
    }

    public RemoteExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(RemoteExecutor executor) {
        this.executor = executor;
    }

    public RpcSerializer getSerializer() {
        return serializer;
    }

    public void setSerializer(RpcSerializer serializer) {
        this.serializer = serializer;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public RpcExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(RpcExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * 反序列化执行
     * 
     * @param rpc
     * @param sender
     * @return
     */
    private RemoteCall deserializeCall(RpcObject rpc, RpcSender sender) {
        try {
            return (RemoteCall) serializer.deserialize(rpc.getData());
        } catch (Exception e) {
            // 出现异常直接返回异常给调用方
            this.handleException(rpc, null, sender, e);
            return null;
        }
    }

    private void handleException(RpcObject rpc, RemoteCall call, RpcSender sender, Exception e) {
        RpcUtils.handleException(exceptionHandler, rpc, call, e);
        if (rpc.getType() == RpcUtils.RpcType.INVOKE) {
            // 生成异常数据
            RpcObject respRpc = this.createRpcObject(rpc.getIndex());
            respRpc.setThreadId(rpc.getThreadId());
            respRpc.setType(RpcUtils.RpcType.FAIL);
            String message = e.getMessage();
            if (message != null) {
                byte[] data = message.getBytes();
                respRpc.setLength(data.length);
                if (data.length > 0) {
                    respRpc.setData(data);
                }
            }
            // 调用失败异常返回
            sender.sendRpcObject(respRpc, timeout);
        }
    }

    private RpcObject createRpcObject(int index) {
        return new RpcObject(0, index, 0, null);
    }
}
