package com.simple.rpc.common.serializer;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public interface RpcSerializer {
    /**
     * 序列化
     * @param obj
     * @return
     */
    public byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes
     * @return
     */
    public Object deserialize(byte[] bytes);

}
