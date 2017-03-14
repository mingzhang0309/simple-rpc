package com.stephen.rpc.oio.client;

import com.stephen.rpc.common.AbstractRpcNetwordBase;
import com.stephen.rpc.common.RpcNetExceptionHandler;
import com.stephen.rpc.common.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class RpcOioConnector extends AbstractRpcNetwordBase implements Service, RpcNetExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcOioConnector.class);

    private Socket socket;
    protected String remoteHost;
    protected int remotePort;
    private DataInputStream dis;
    private DataOutputStream dos;
    protected boolean stop = false;

    public RpcOioConnector() {
    }

    public RpcOioConnector(Socket socket){
        this.socket = socket;
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

        } catch (Exception e) {
            handleNetException(e);
        }
    }

    @Override
    public void stopService() {

    }

    @Override
    public void handleNetException(Exception e) {

    }

    private class ClientThread extends Thread {
        @Override
        public void run() {
            while (!stop) {

            }
        }
    }
}
