package com.stephen.rpc.oio;

import com.stephen.rpc.oio.client.RpcOioConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcOioConnectorTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcOioConnectorTest.class);
    private String host = "127.0.0.1";
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

    }

    @Test
    public void testStopService() throws Exception {

    }

    @Test
    public void testHandleNetException() throws Exception {

    }

    public static void main(String[] args) {
        RpcOioConnector rpcOioConnector = new RpcOioConnector();
        rpcOioConnector.setHost("127.0.0.1");
        rpcOioConnector.setPort(5566);
        rpcOioConnector.startService();
    }
}