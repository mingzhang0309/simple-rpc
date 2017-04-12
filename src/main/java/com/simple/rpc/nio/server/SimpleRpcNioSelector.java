package com.simple.rpc.nio.server;

import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.nio.client.AbstractRpcConnector;
import com.simple.rpc.nio.client.RpcNioBuffer;
import com.simple.rpc.nio.client.RpcNioConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private LinkedList<Runnable> selectTasks = new LinkedList<Runnable>();
    private ConcurrentHashMap<SocketChannel,RpcNioConnector> connectorCache;
    private ConcurrentHashMap<ServerSocketChannel,RpcNioAcceptor> acceptorCache;

    public SimpleRpcNioSelector() {
        try {
            selector = Selector.open();
            connectors = new CopyOnWriteArrayList<RpcNioConnector>();
            connectorCache = new ConcurrentHashMap<SocketChannel, RpcNioConnector>();
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }

    @Override
    public void register(final RpcNioAcceptor acceptor) {
        final ServerSocketChannel channel = acceptor.getServerSocketChannel();
        this.addSelectTask(new Runnable() {
            public void run() {
                try {
                    channel.register(selector, SelectionKey.OP_ACCEPT);
                } catch (Exception e) {
                    acceptor.handleNetException(e);
                }
            }
        });
        this.notifySend(null);
    }

    @Override
    public void unRegister(RpcNioAcceptor acceptor) {

    }

    @Override
    public void register(final RpcNioConnector connector) {
        this.addSelectTask(new Runnable() {
            public void run() {
                try {
                    SelectionKey selectionKey = connector.getChannel().register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    connector.handleNetException(e);
                }
            }
        });
        connectors.add(connector);
        connectorCache.put(connector.getChannel(), connector);
    }

    private void addSelectTask(Runnable task){
        selectTasks.offer(task);
    }

    private boolean hasTask(){
        Runnable peek = selectTasks.peek();
        return peek!=null;
    }

    private void runSelectTasks(){
        Runnable peek = selectTasks.peek();
        while(peek!=null){
            peek = selectTasks.pop();
            peek.run();
            peek = selectTasks.peek();
        }
    }

    @Override
    public void unRegister(RpcNioConnector connector) {
    }

    @Override
    public void handleNetException(Exception e) {
        logger.error("selector exception", e);
    }

    @Override
    public void notifySend(AbstractRpcConnector connector) {
        selector.wakeup();
    }

    @Override
    public void startService() {
        if(!started.get()) {
            new SelectionThread("selection thread").start();
            started.set(true);
        }
    }

    @Override
    public void stopService() {

    }

    private class SelectionThread extends Thread {
        public SelectionThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            logger.info("select thread has started :"+Thread.currentThread().getId());
            while (!stop.get()) {
                if(SimpleRpcNioSelector.this.hasTask()){
                    logger.info("run task");
                    SimpleRpcNioSelector.this.runSelectTasks();
                } else {
                    logger.info("run not task");
                }
                boolean needSend = checkSend();
                logger.info("needSend {}", needSend);
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
                logger.info("selectionKeys {}", selectionKeys);
                for (SelectionKey selectionKey : selectionKeys) {
                    doDispatchSelectionKey(selectionKey);
                }
            }
        }
    }

    private boolean checkSend(){
        boolean needSend = false;
        for (RpcNioConnector connector : connectors) {
            if (connector.isNeedToSend()) {
                SelectionKey selectionKey = connector.getChannel().keyFor(selector);
                selectionKey.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
                needSend = true;
            }
        }
        return needSend;
    }

    private boolean doDispatchSelectionKey(SelectionKey selectionKey){
        logger.info("selectionKey {}", selectionKey);
        boolean result = false;
        try{
            if (selectionKey.isAcceptable()) {
                result = doAccept(selectionKey);
            }
            if (selectionKey.isReadable()) {
                result = doRead(selectionKey);
            }
//            if (selectionKey.isWritable()) {
//                result = doWrite(selectionKey);
//            }
        }catch(Exception e){
            this.handSelectionKeyException(selectionKey, e);
        }
        return result;
    }

    private boolean doAccept(SelectionKey selectionKey){
        logger.info("selectionKey accept {}", selectionKey);
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel)selectionKey.channel();
//        RpcNioAcceptor acceptor = acceptorCache.get(server);
        try{
            SocketChannel client = serverSocketChannel.accept();
            if(client!=null){
                client.configureBlocking(false);
//                if(delegageSelector!=null){
//                    RpcNioConnector connector = new RpcNioConnector(client,delegageSelector);
//                    connector.setAcceptor(acceptor);
//                    connector.setExecutorService(acceptor.getExecutorService());
//                    connector.setExecutorSharable(true);
//                    delegageSelector.register(connector);
//                    connector.startService();
//                }else{
                RpcNioConnector connector = new RpcNioConnector(client, this);
                // connector.setAcceptor(acceptor);
                // connector.setExecutorService(acceptor.getExecutorService());
                // connector.setExecutorSharable(true);
                this.register(connector);
                connector.startService();
//                }
                return true;
            }
        }catch(Exception e){
            this.handSelectionKeyException(selectionKey, e);
        }
        return false;
    }

    private boolean doRead(SelectionKey selectionKey){
        logger.info("selectionKey read {}", selectionKey);
        boolean result = false;
        SocketChannel client = (SocketChannel) selectionKey.channel();
        RpcNioConnector connector = connectorCache.get(client);
        if (connector != null) {
            try {
                RpcNioBuffer connectorReadBuf = connector.getRpcNioReadBuffer();
                ByteBuffer channelReadBuf = connector.getChannelReadBuffer();
                while (!stop.get()) {
                    int read = 0;
                    while ((read = client.read(channelReadBuf)) > 0) {
                        channelReadBuf.flip();
                        byte[] readBytes = new byte[read];
                        channelReadBuf.get(readBytes);
                        connectorReadBuf.write(readBytes);
                        channelReadBuf.clear();
                        while (connectorReadBuf.hasRpcObject()) {
                            RpcObject rpc = connectorReadBuf.readRpcObject();
                            this.fireRpc(connector, rpc);
                        }
                    }
                    if (read < 1) {
                        if (read < 0) {
                            this.handSelectionKeyException(selectionKey, new RpcException());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                this.handSelectionKeyException(selectionKey, e);
            }
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

    private void fireRpc(RpcNioConnector connector,RpcObject rpc){
        rpc.setHost(connector.getRemoteHost());
        rpc.setPort(connector.getRemotePort());
        rpc.setRpcContext(connector.getRpcContext());
        connector.fireCall(rpc);
    }
}
