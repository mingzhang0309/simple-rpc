package com.simple.rpc.common;

import com.simple.rpc.common.exception.RpcException;

/**
 * Created by stephen.zhang on 17/3/29.
 */
public class SimpleFutureRpcSync implements RpcSync {
    @Override
    public void waitForResult(int time, RpcCallSync sync) {
        int timeAll = 0;
        while (!sync.isDone()) {
            try {
                Thread.currentThread().sleep(1000);
                timeAll+=1000;
                if(timeAll > time) {
                    throw new RpcException("request time out");
                }
            } catch (InterruptedException e) {
                throw new RpcException(e);
            }
        }
    }

    @Override
    public void notifyResult(RpcCallSync sync, RpcObject rpc) {
        if(sync != null) {
            sync.setResponse(rpc);
        }
    }
}
