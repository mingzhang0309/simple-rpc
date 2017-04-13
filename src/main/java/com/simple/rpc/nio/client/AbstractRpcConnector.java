package com.simple.rpc.nio.client;

import com.simple.rpc.common.RpcNetBase;
import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.RpcSender;
import com.simple.rpc.common.Service;
import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.nio.server.RpcOutputNofity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by stephen.zhang on 17/4/10.
 */
public abstract class AbstractRpcConnector extends RpcNetBase implements Service,RpcSender {
    private Logger logger = LoggerFactory.getLogger(AbstractRpcConnector.class);
    protected boolean stop = false;
    protected String remoteHost;
    protected int remotePort;
    protected ConcurrentHashMap<String,Object> rpcContext;
    private RpcOutputNofity outputNotify;
    protected ConcurrentLinkedQueue<RpcObject> sendQueueCache = new ConcurrentLinkedQueue<RpcObject>();
    //写线程
    private AbstractRpcWriter rpcWriter;

    public AbstractRpcConnector() {
    }

    public AbstractRpcConnector(AbstractRpcWriter rpcWriter){
        super();
        this.rpcWriter = rpcWriter;
        rpcContext = new ConcurrentHashMap<String,Object>();
    }


    public ConcurrentHashMap<String, Object> getRpcContext() {
        return rpcContext;
    }

    public void setRpcContext(ConcurrentHashMap<String, Object> rpcContext) {
        this.rpcContext = rpcContext;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public boolean isNeedToSend(){
        RpcObject peek = sendQueueCache.peek();
        return peek!=null;
    }

    public RpcObject getToSend(){
        return sendQueueCache.poll();
    }

    @Override
    public boolean sendRpcObject(RpcObject rpc, int timeout) {
        int cost = 0;
        while (!sendQueueCache.offer(rpc)) {
            cost += 3;
            try {
                Thread.currentThread().sleep(3);
            } catch (InterruptedException e) {
                throw new RpcException(e);
            }
            if (timeout > 0 && cost > timeout) {
                throw new RpcException("request time out");
            }
        }
        this.notifySend();
        return true;
    }

    public void notifySend(){
        if (rpcWriter != null) {
            rpcWriter.notifySend(this);
        }
    }

    public AbstractRpcWriter getRpcWriter() {
        return rpcWriter;
    }

    public void setRpcWriter(AbstractRpcWriter rpcWriter) {
        this.rpcWriter = rpcWriter;
    }

    public RpcOutputNofity getOutputNotify() {
        return outputNotify;
    }

    public void setOutputNotify(RpcOutputNofity outputNotify) {
        this.outputNotify = outputNotify;
    }

    public boolean isStop() {
        return stop;
    }

    public abstract void handleConnectorException(Exception e);

    @Override
    public final void handleNetException(Exception e) {
        this.handleConnectorException(e);
    }

    public void fireCall(final RpcObject rpc){
        fireCallListeners(rpc, AbstractRpcConnector.this);
    }

}
