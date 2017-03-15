package com.simple.rpc.oio;

import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.RpcUtils;
import com.simple.rpc.common.Service;
import com.simple.rpc.oio.client.RpcOioConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class SimpleRpcOioWriter implements Service {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRpcOioWriter.class);
    private boolean stop = false;
    private int interval = 50;
    private AtomicBoolean started = new AtomicBoolean(false);
    protected Thread sendThread;

    private List<RpcOioConnector> connectors;

    public SimpleRpcOioWriter() {
        connectors = new CopyOnWriteArrayList<RpcOioConnector>();
    }

    public void registerWrite(RpcOioConnector connector) {
        connectors.add(connector);
    }

    public void unRegWrite(RpcOioConnector connector) {
        connectors.remove(connector);
    }

    public boolean exeSend(RpcOioConnector con){
        boolean hasSend = false;
        RpcOioConnector connector = (RpcOioConnector)con;
        DataOutputStream dos = connector.getOutputStream();
        while(connector.isNeedToSend()){
            RpcObject rpc = connector.getToSend();
            logger.info("发送rpcObject {}", rpc);
            RpcUtils.writeDataRpc(rpc, dos, connector);
            hasSend = true;
        }
        return hasSend;
    }

    @Override
    public void startService() {
        if(!started.get()){
            sendThread = new WriteThread();
            sendThread.start();
            started.set(true);
        }
    }

    @Override
    public void stopService() {
        stop = true;
        sendThread.interrupt();
    }

    public void notifySend(RpcOioConnector rpcOioConnector) {
        sendThread.interrupt();
    }

    private class WriteThread extends Thread{
        @Override
        public void run() {
            boolean hasSend = false;
            logger.info("SimpleRpcOioWriter start");
            while(!stop){
                try {
                    for(RpcOioConnector connector:connectors){
                        hasSend |= exeSend(connector);
                    }
                    if(!hasSend){
                        Thread.currentThread().sleep(interval);
                    }
                    hasSend = false;
                } catch (InterruptedException e) {
                    //notify to send
                }
            }
        }
    }
}
