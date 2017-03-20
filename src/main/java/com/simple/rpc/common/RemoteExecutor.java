package com.simple.rpc.common;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public interface RemoteExecutor extends Service {
    /**
     * 无返回值执行，无论是否异常
     * @param call
     */
    public void oneway(RemoteCall call);

    /**
     * 有返回值执行
     * @param call
     * @return
     */
    public Object invoke(RemoteCall call);

    public static final int ONEWAY = RpcUtils.RpcType.ONEWAY.getType();

    public static final int INVOKE = RpcUtils.RpcType.INVOKE.getType();
}
