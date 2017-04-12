package com.simple.rpc.nio.server;

import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.nio.client.AbstractRpcConnector;
import com.simple.rpc.nio.client.RpcNioConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by stephen.zhang on 17/4/10.
 */
public class SimpleRpcNioSelector extends AbstractRpcNioSelector {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRpcNioSelector.class);

    private AtomicBoolean started = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);
    private Selector selector;
    private List<RpcNioConnector> connectors;
    private ConcurrentHashMap<SocketChannel,RpcNioConnector> connectorCache;
    private ConcurrentHashMap<ServerSocketChannel,RpcNioAcceptor> acceptorCache;

    public SimpleRpcNioSelector() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void register(RpcNioAcceptor acceptor) {

    }

    @Override
    public void unRegister(RpcNioAcceptor acceptor) {

    }

    @Override
    public void register(RpcNioConnector connector) {
        try {
            SelectionKey selectionKey = connector.getChannel().register(selector,SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            connector.handleNetException(e);
        }
    }

    @Override
    public void unRegister(RpcNioConnector connector) {
    }

    @Override
    public void handleNetException(Exception e) {

    }

    @Override
    public void notifySend(AbstractRpcConnector connector) {

    }

    @Override
    public void startService() {
        if(!started.get()) {
            new SelectionThread().start();
            started.set(true);
        }
    }

    @Override
    public void stopService() {

    }

    private class SelectionThread extends Thread {
        @Override
        public void run() {
            logger.info("select thread has started :"+Thread.currentThread().getId());
            while (!stop.get()) {
                boolean needSend = checkSend();
                try {
                    if (needSend) {
                        selector.selectNow();
                    } else {
                        selector.select();
                    }
                } catch (IOException e) {
                    SimpleRpcNioSelector.this.handleNetException(e);
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    doDispatchSelectionKey(selectionKey);
                }
            }
        }
    }

    private boolean checkSend(){
        boolean needSend = false;
        for (RpcNioConnector connector : connectors) {
//            if (connector.isNeedToSend()) {
//                SelectionKey selectionKey = connector.getChannel().keyFor(selector);
//                selectionKey.interestOps(READ_WRITE_OP);
//                needSend = true;
//            }
        }
        return needSend;
    }

    private boolean doDispatchSelectionKey(SelectionKey selectionKey){
        boolean result = false;
        try{
//            if (selectionKey.isAcceptable()) {
//                result = doAccept(selectionKey);
//            }
//            if (selectionKey.isReadable()) {
//                result = doRead(selectionKey);
//            }
//            if (selectionKey.isWritable()) {
//                result = doWrite(selectionKey);
//            }
        }catch(Exception e){
            this.handSelectionKeyException(selectionKey, e);
        }
        return result;
    }

    private void handSelectionKeyException(final SelectionKey selectionKey,Exception e){
        SelectableChannel channel = selectionKey.channel();
        if(channel instanceof ServerSocketChannel){
            RpcNioAcceptor acceptor = acceptorCache.get(channel);
            if(acceptor!=null){
                logger.error("acceptor " + acceptor.getHost() + ":" + acceptor.getPort() + " selection error " + e.getClass() + " " + e.getMessage() + " start to shutdown");
                acceptor.stopService();
            }
        }else{
            RpcNioConnector connector = connectorCache.get(channel);
            if(connector!=null){
                logger.error("connector " + connector.getHost() + ":" + connector.getPort() + " selection error " + e.getClass() + " " + e.getMessage() + " start to shutdown");
                connector.stopService();
            }
        }
    }
}
