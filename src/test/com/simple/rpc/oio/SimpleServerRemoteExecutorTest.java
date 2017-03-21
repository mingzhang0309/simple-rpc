package com.simple.rpc.oio;

import com.simple.rpc.oio.server.RpcOioAcceptor;
import com.simple.rpc.oio.server.SimpleServerRemoteExecutor;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class SimpleServerRemoteExecutorTest {
    public static void main(String[] args) {
        RpcOioAcceptor rpcOioAcceptor = new RpcOioAcceptor();
        rpcOioAcceptor.setHost("127.0.0.1");
        rpcOioAcceptor.setPort(5566);

        SimpleServerRemoteExecutor executor = new SimpleServerRemoteExecutor();

        LoginRpcServiceImpl loginRpcService = new LoginRpcServiceImpl();
        executor.registerRemote(LoginRpcService.class, loginRpcService);

        rpcOioAcceptor.startService();
    }
}
