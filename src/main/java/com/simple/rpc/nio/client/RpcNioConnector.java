package com.simple.rpc.nio.client;

import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.RpcUtils;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.nio.server.AbstractRpcNioSelector;
import com.simple.rpc.nio.server.RpcNioAcceptor;
import com.simple.rpc.nio.server.SimpleRpcNioSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by stephen.zhang on 17/4/10.
 */
public class RpcNioConnector extends AbstractRpcConnector {
    private static final Logger logger = LoggerFactory.getLogger(RpcNioConnector.class);

    private AbstractRpcNioSelector selector;
    private SelectionKey selectionKey;
    private SocketChannel channel;
    private ByteBuffer channelWriteBuffer;
    private ByteBuffer channelReadBuffer;
    private RpcNioBuffer rpcNioReadBuffer;
    private RpcNioBuffer rpcNioWriteBuffer;

    public RpcNioConnector(){
        this(null);
    }

    public RpcNioConnector(SocketChannel socketChanel,AbstractRpcNioSelector selection){
        this(selection);
        this.channel = socketChanel;
    }

    public RpcNioConnector(AbstractRpcNioSelector selector){
        super(null);
        if(selector==null){
            this.selector = new SimpleRpcNioSelector();
        }else{
            this.selector = selector;
        }
        this.initBuf();
    }

    private void initBuf(){
        channelWriteBuffer = ByteBuffer.allocate(RpcUtils.MEM_512KB);
        channelReadBuffer = ByteBuffer.allocate(RpcUtils.MEM_512KB);
        rpcNioReadBuffer = new RpcNioBuffer(RpcUtils.MEM_512KB);
        rpcNioWriteBuffer = new RpcNioBuffer(RpcUtils.MEM_512KB);
    }

    @Override
    public void startService() {
        try{
            if (channel == null) {
                channel = SocketChannel.open();
                channel.connect(new InetSocketAddress(this.getHost(), this.getPort()));
                channel.configureBlocking(false);
                while (!channel.isConnected())
                    ;
                logger.info("connect to " + this.getHost() + ":" + this.getPort() + " success");
                selector.startService();
                selector.register(this);
            }
            InetSocketAddress remoteAddress = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
            InetSocketAddress localAddress = (InetSocketAddress)channel.socket().getLocalSocketAddress();
            //fix jdk 1.6 not support
            //InetSocketAddress remoteAddress = (InetSocketAddress)channel.getRemoteAddress();
            //InetSocketAddress localAddress = (InetSocketAddress)channel.getLocalAddress();
            String remote = RpcUtils.genAddressString("remoteAddress-> ", remoteAddress);
            String local = RpcUtils.genAddressString("localAddress-> ", localAddress);
            logger.info(local+"  "+remote);
            remotePort = remoteAddress.getPort();
            remoteHost = remoteAddress.getAddress().getHostAddress();
        }catch(IOException e){
            logger.error("connect to host "+this.getHost()+" port "+this.getPort()+" failed", e);
            throw new RpcException("connect to host error");
        }
    }

    public ByteBuffer getChannelWriteBuffer() {
        return channelWriteBuffer;
    }

    public ByteBuffer getChannelReadBuffer() {
        return channelReadBuffer;
    }

    @Override
    public void stopService() {
        this.selector.unRegister(this);
        this.sendQueueCache.clear();
        this.rpcContext.clear();
        try {
            channel.close();
            channelWriteBuffer.clear();
            channelReadBuffer.clear();
            rpcNioReadBuffer.clear();
            rpcNioWriteBuffer.clear();
        } catch (IOException e) {

        }
        this.stop = true;
    }


    public boolean isValid(){
        return !stop;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    public void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    @Override
    public void notifySend() {
        selector.notifySend(this);
    }

    public RpcNioBuffer getRpcNioReadBuffer() {
        return rpcNioReadBuffer;
    }

    public RpcNioBuffer getRpcNioWriteBuffer() {
        return rpcNioWriteBuffer;
    }

    @Override
    public void handleConnectorException(Exception e) {
        logger.error("connector "+this.getHost()+":"+this.getPort()+" io exception start to shutdown");
        this.stopService();
    }
}
