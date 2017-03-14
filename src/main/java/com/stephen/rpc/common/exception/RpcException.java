package com.stephen.rpc.common.exception;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class RpcException extends RuntimeException {
    private static final long serialVersionUID = -9023105076586140802L;

    public RpcException(){
        super();
    }

    public RpcException(String message){
        super(message);
    }

    public RpcException(Throwable thr){
        super(thr);
    }
}
