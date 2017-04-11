package com.simple.rpc.oio.client;

import com.simple.rpc.common.*;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.nio.client.AbstractRpcConnector;
import com.simple.rpc.oio.SimpleRpcOioWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class RpcOioConnector extends AbstractRpcConnector implements RpcSender {
    private static final Logger logger = LoggerFactory.getLogger(RpcOioConnector.class);

    private Socket socket;
    protected String remoteHost;
    protected int remotePort;
    private DataInputStream dis;
    private DataOutputStream dos;
    protected boolean stop = false;
    private SimpleRpcOioWriter simpleRpcOioWriter;
    protected ConcurrentLinkedQueue<RpcObject> sendQueueCache = new ConcurrentLinkedQueue<RpcObject>();

    public RpcOioConnector() {
        init();
    }

    public RpcOioConnector(Socket socket){
        this.socket = socket;
        init();
    }

    public void init() {
        if(simpleRpcOioWriter == null) {
            simpleRpcOioWriter = new SimpleRpcOioWriter();
        }
    }

    @Override
    public void startService() {
        try {
            if(socket == null) {
                socket = new Socket();
                socket.connect(new InetSocketAddress(getHost(), getPort()));
            }
            InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
            remoteHost = remoteAddress.getAddress().getHostAddress();
            remotePort = remoteAddress.getPort();
            logger.info("连接远程服务成功 {} {}", remoteHost, remotePort);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            simpleRpcOioWriter.registerWrite(this);
            simpleRpcOioWriter.startService();
            new ClientThread().start();
        } catch (Exception e) {
            handleNetException(e);
        }
    }

    @Override
    public void stopService() {
        stop = true;
        RpcUtils.close(dis, dos);
        try {
            socket.close();
        } catch (IOException e) {
            //do nothing
        }
        sendQueueCache.clear();
        simpleRpcOioWriter.unRegWrite(this);
    }

    public DataOutputStream getOutputStream() {
        return dos;
    }

    public boolean isNeedToSend() {
        RpcObject peek = sendQueueCache.peek();
        return peek!=null;
    }

    public RpcObject getToSend() {
        return sendQueueCache.poll();
    }

    private class ClientThread extends Thread {
        @Override
        public void run() {
            while (!stop) {
                RpcObject rpc = RpcUtils.readDataRpc(dis, RpcOioConnector.this);
                if(rpc!=null){
                    rpc.setHost(remoteHost);
                    rpc.setPort(remotePort);
//                    rpc.setRpcContext(rpcContext);
                    fireCall(rpc);
                }
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
            }
        }
    }

    public void notifySend(){
        if(simpleRpcOioWriter!=null){
            simpleRpcOioWriter.notifySend(this);
        }
    }

    @Override
    public void handleConnectorException(Exception e) {

    }

    public void fireCall(final RpcObject rpc){
        fireCallListeners(rpc, this);
    }

}
