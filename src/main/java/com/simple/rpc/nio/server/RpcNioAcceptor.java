package com.simple.rpc.nio.server;

import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.common.server.AbstractRpcAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by stephen.zhang on 17/4/6.
 */
public class RpcNioAcceptor extends AbstractRpcAcceptor {
    private static final Logger logger = LoggerFactory.getLogger(RpcNioAcceptor.class);

    private ServerSocketChannel serverSocketChannel;
    private AbstractRpcNioSelector selector;

    public RpcNioAcceptor(AbstractRpcNioSelector selector) {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            this.selector = selector;
        } catch (Exception e) {
            this.handleNetException(e);
        }
    }

    public RpcNioAcceptor() {
        this(null);
    }

    @Override
    public void startService() {
        super.startService();
        try {
            if(selector==null){
                selector = new SimpleRpcNioSelector();
            }
            selector.startService();
            serverSocketChannel.socket().bind(new InetSocketAddress(this.getHost(), this.getPort()));
            selector.register(this);
            this.startListeners();
        } catch (Exception e) {
            this.handleNetException(e);
        }
    }

    @Override
    public void stopService() {
        super.stopService();
        if(serverSocketChannel!=null){
            try {
                serverSocketChannel.close();
                if(selector!=null){
                    selector.stopService();
                }
            } catch (IOException e) {
            }
        }
        this.stopListeners();
    }

    @Override
    public void handleNetException(Exception e) {
        logger.error("nio acceptor io exception,start to shut down service");
        this.stopService();
        throw new RpcException(e);
    }

    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }
}
