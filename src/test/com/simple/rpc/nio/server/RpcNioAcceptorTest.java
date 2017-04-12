package com.simple.rpc.nio.server;

import com.simple.rpc.common.server.AbstractRpcAcceptor;
import com.simple.rpc.common.server.RpcServiceProvider;
import com.simple.rpc.oio.LoginRpcService;
import com.simple.rpc.oio.LoginRpcServiceImpl;
import com.simple.rpc.oio.server.SimpleServerRemoteExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcNioAcceptorTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcNioAcceptorTest.class);

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 4332;

        AbstractRpcAcceptor acceptor = new RpcNioAcceptor();
        acceptor.setHost(host);
        acceptor.setPort(port);

        SimpleServerRemoteExecutor simpleServerRemoteExecutor = new SimpleServerRemoteExecutor();
        RpcServiceProvider provider = new RpcServiceProvider(simpleServerRemoteExecutor);

        LoginRpcService loginService = new LoginRpcServiceImpl();
        simpleServerRemoteExecutor.registerRemote(LoginRpcService.class, loginService);

        acceptor.addRpcCallListener(provider);
        acceptor.startService();
        logger.info("service started");
    }
}