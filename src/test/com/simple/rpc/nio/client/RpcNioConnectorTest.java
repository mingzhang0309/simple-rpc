package com.simple.rpc.nio.client;

import com.simple.rpc.oio.LoginRpcService;
import com.simple.rpc.oio.client.SimpleClientRemoteExecutor;
import com.simple.rpc.oio.client.SimpleClientRemoteProxy;

import static org.junit.Assert.*;

public class RpcNioConnectorTest {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 4332;
        AbstractRpcConnector connector = new RpcNioConnector(null);
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