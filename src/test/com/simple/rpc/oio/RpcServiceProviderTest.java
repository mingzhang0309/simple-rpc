package com.simple.rpc.oio;

import com.simple.rpc.common.server.RpcServiceProvider;
import com.simple.rpc.oio.server.RpcOioAcceptor;
import com.simple.rpc.oio.server.SimpleServerRemoteExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class RpcServiceProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceProviderTest.class);

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 5566;

        RpcOioAcceptor acceptor = new RpcOioAcceptor();
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
