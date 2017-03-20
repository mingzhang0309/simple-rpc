package com.simple.rpc.common.serializer;

import com.simple.rpc.common.exception.RpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by stephen.zhang on 17/3/20.
 */
public class JdkSerializer implements RpcSerializer {

    /**
     * 先序列化再执行压缩，减少网络流量
     */
    @Override
    public byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bis = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(bis);
            stream.writeObject(obj);
            stream.close();
            byte[] bytes = bis.toByteArray();
            //使用zip压缩，缩小网络包
            return NioUtils.zip(bytes);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    /**
     * 先解压缩，再反序列化
     */
    @Override
    public Object deserialize(byte[] bytes) {
        try {
            //使用zip解压缩
            byte[] unzip = NioUtils.unzip(bytes);
            ByteArrayInputStream bis = new ByteArrayInputStream(unzip);
            ObjectInputStream stream = new ObjectInputStream(bis);
            return stream.readObject();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

}