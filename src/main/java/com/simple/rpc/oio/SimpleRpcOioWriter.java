package com.simple.rpc.oio;

import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.RpcUtils;
import com.simple.rpc.common.Service;
import com.simple.rpc.nio.client.AbstractRpcConnector;
import com.simple.rpc.nio.client.AbstractRpcWriter;
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
public class SimpleRpcOioWriter extends AbstractRpcWriter implements Service {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRpcOioWriter.class);

    public boolean exeSend(AbstractRpcConnector con){
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
    public boolean doSend(AbstractRpcConnector connector) {
        return exeSend(connector);
    }
}
