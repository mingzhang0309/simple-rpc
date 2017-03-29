package com.simple.rpc.oio;

import com.simple.rpc.oio.client.RpcOioConnector;
import com.simple.rpc.oio.client.SimpleClientRemoteExecutor;
import com.simple.rpc.oio.client.SimpleClientRemoteProxy;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public class SimpleClientRemoteExecutorTest {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 5566;
        RpcOioConnector connector = new RpcOioConnector(null);
        connector.setHost(host);
        connector.setPort(port);

        SimpleClientRemoteExecutor executor = new SimpleClientRemoteExecutor(connector);
        SimpleClientRemoteProxy proxy = new SimpleClientRemoteProxy();
        proxy.setRemoteExecutor(executor);
        proxy.startService();

        LoginRpcService loginService = proxy.registerRemote(LoginRpcService.class);
        System.out.println(loginService.login("linda", "123456"));
    }
}
