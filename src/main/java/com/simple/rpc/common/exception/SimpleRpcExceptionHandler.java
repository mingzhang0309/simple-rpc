package com.simple.rpc.common.exception;

import com.simple.rpc.common.RemoteCall;
import com.simple.rpc.common.RpcObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class SimpleRpcExceptionHandler implements RpcExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRpcExceptionHandler.class);

    @Override
    public void handleException(RpcObject rpc,RemoteCall call,Throwable e) {
        if(e instanceof RpcException){
            logger.info("rpcException "+e.getMessage());
        }else{
            e.printStackTrace();
        }
    }
}
