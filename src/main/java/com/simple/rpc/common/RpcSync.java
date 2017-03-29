package com.simple.rpc.common;

/**
 * Created by stephen.zhang on 17/3/29.
 */
public interface RpcSync {
    /**
     * 同步等待执行结果
     * @param time
     * @param sync
     */
    public void waitForResult(int time,RpcCallSync sync);

    /**
     * 通知结果返回
     * @param sync
     * @param rpc 返回值
     */
    public void notifyResult(RpcCallSync sync,RpcObject rpc);
}
