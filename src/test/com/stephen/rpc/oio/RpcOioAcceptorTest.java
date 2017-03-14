package com.stephen.rpc.oio;

import com.stephen.rpc.oio.server.RpcOioAcceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcOioAcceptorTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcOioAcceptorTest.class);
    private String host;
    private int port;

    @Before
    public void setUp() throws Exception {
        host = "127.0.0.1";
        port = 5566;
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testStartService() throws Exception {
        RpcOioAcceptor rpcOioAcceptor = new RpcOioAcceptor();
        rpcOioAcceptor.setHost(host);
        rpcOioAcceptor.setPort(port);
        rpcOioAcceptor.startService();
    }

    @Test
    public void testStopService() throws Exception {

    }

    @Test
    public void testHandleNetException() throws Exception {

    }


    public static void main(String[] args) {
        RpcOioAcceptor rpcOioAcceptor = new RpcOioAcceptor();
        rpcOioAcceptor.setHost("127.0.0.1");
        rpcOioAcceptor.setPort(5566);
        rpcOioAcceptor.startService();
    }
}