package com.simple.rpc.oio.server;

import com.simple.rpc.common.*;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.oio.client.RpcOioConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class RpcOioAcceptor extends RpcNetBase implements RpcNetExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcOioAcceptor.class);

    private ServerSocket server;

    protected boolean stop = false;

    @Override
    public void startService() {
        try {
            if(server == null) {
                server = new ServerSocket();
                server.bind(new InetSocketAddress(getHost(), getPort()));
                new AcceptThread().start();
            }
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void stopService() {

    }

    private class AcceptThread extends Thread {
        @Override
        public void run() {
            while (!stop) {
                try {
                    logger.info("轮训线程启动完毕,阻塞在accept{} {}", getHost(), getPort());
                    Socket socket = server.accept();
                    logger.info("接收连接 {}", socket.getRemoteSocketAddress());
                    RpcOioConnector connector = new RpcOioConnector(socket);
                    RpcOioAcceptor.this.addConnectorListeners(connector);
                    connector.startService();
                } catch (IOException e) {
                    handleNetException(e);
                }
            }
        }
    }

    @Override
    public void handleNetException(Exception e) {
        logger.error("oio acceptor exception", e);
        this.stopService();
    }
}
