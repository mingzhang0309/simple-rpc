package com.simple.rpc.common;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class RpcObject {
    /**
     * rpc类型 协议字段
     */
    private RpcUtils.RpcType type;

    /**
     * 请求线程ID，用于包回调通知  协议字段
     */
    private long threadId;

    /**
     * 请求线程类seq，用于标记请求发送 协议字段
     */
    private int index;

    /**
     * data数据结构长度  协议字段
     */
    private int length;

    /**
     * 请求body  协议字段,RemoteCall序列化后存储
     */
    private byte[] data = new byte[0];

    /**
     * 请求发送方，用于网络传递
     */
    private String host;

    /**
     *  请求发送方，用于网络传递
     */
    private int port;

    /**
     * 请求上线文参数加入传递
     */
    private ConcurrentHashMap<String,Object> rpcContext;

    public RpcObject(){

    }

    public RpcObject(int type,int index,int len,byte[] data){
        this.type = RpcUtils.RpcType.getByType(type);
        this.index = index;
        this.length = len;
        this.data = data;
        this.threadId = Thread.currentThread().getId();
    }

    public RpcUtils.RpcType getType() {
        return type;
    }

    public void setType(RpcUtils.RpcType type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, Object> getRpcContext() {
        return rpcContext;
    }

    public void setRpcContext(ConcurrentHashMap<String, Object> rpcContext) {
        this.rpcContext = rpcContext;
    }

    @Override
    public String toString() {
        return "RpcObject{" +
                "type=" + type +
                ", threadId=" + threadId +
                ", index=" + index +
                ", length=" + length +
                ", data=" + Arrays.toString(data) +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", rpcContext=" + rpcContext +
                '}';
    }
}
