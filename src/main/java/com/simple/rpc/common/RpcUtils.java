package com.simple.rpc.common;

import com.simple.rpc.common.exception.RpcException;
import com.simple.rpc.common.exception.RpcExceptionHandler;
import com.simple.rpc.oio.client.RpcOioConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephen.zhang on 17/3/14.
 */
public class RpcUtils {
    private static final Logger logger = LoggerFactory.getLogger(RpcUtils.class);

    private static Map<String, Method> methodCache = new HashMap<String, Method>();
    public static int MEM_8KB = 1024*8;

    public static int MEM_16KB = MEM_8KB*2;

    public static int MEM_32KB = MEM_16KB*2;

    public static int MEM_64KB = MEM_32KB*2;

    public static int MEM_128KB = MEM_64KB*2;

    public static int MEM_256KB = MEM_128KB*2;

    public static int MEM_512KB = MEM_256KB*2;

    public static int MEM_1M = MEM_512KB*2;

    public static String DEFAULT_VERSION = "0.0";

    public static String DEFAULT_GROUP = "default";

    public static void writeDataRpc(RpcObject rpc, DataOutputStream dos,RpcNetExceptionHandler handler) {
        try {
            dos.writeInt(rpc.getType().getType());
            dos.writeLong(rpc.getThreadId());
            dos.writeInt(rpc.getIndex());
            dos.writeInt(rpc.getLength());
            if (rpc.getLength() > 0) {
                if (rpc.getLength() > MEM_512KB) {
                    throw new RpcException("rpc data too long "+ rpc.getLength());
                }
                dos.write(rpc.getData());
            }
            dos.flush();
        } catch (IOException e) {
            handleNetException(e,handler);
        }
    }

    private static void handleNetException(Exception e,RpcNetExceptionHandler handler){
        if(handler!=null){
            handler.handleNetException(e);
        }else{
            throw new RpcException(e);
        }
    }

    public static RpcObject readDataRpc(DataInputStream dis, RpcOioConnector handler) {
        try {
            RpcObject rpc = new RpcObject();
            rpc.setType(RpcType.getByType(dis.readInt()));
            rpc.setThreadId(dis.readLong());
            rpc.setIndex(dis.readInt());
            rpc.setLength(dis.readInt());
            if (rpc.getLength() > 0) {
                if (rpc.getLength() > MEM_512KB) {
                    throw new RpcException("rpc data too long "+ rpc.getLength());
                }
                byte[] buf = new byte[rpc.getLength()];
                dis.read(buf);
                rpc.setData(buf);
            }
            return rpc;
        } catch (IOException e) {
            handleNetException(e,handler);
            return null;
        }
    }

    public static void close(DataInputStream dis, DataOutputStream dos) {
        try {
            dis.close();
            dos.close();
        } catch (IOException e) {
            // close all
        }
    }

    public enum RpcType {
        ONEWAY(1), INVOKE(2), SUC(3), FAIL(4);
        private int type;

        RpcType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static RpcType getByType(int type) {
            RpcType[] values = RpcType.values();
            for (RpcType v : values) {
                if (v.type == type) {
                    return v;
                }
            }
            return ONEWAY;
        }
    }

    /**
     * 调用对象的方法执行
     * @param obj
     * @param methodName
     * @param args
     * @param exceptionHandler
     * @return
     */
    public static Object invokeMethod(Object obj, String methodName,Object[] args,RpcExceptionHandler exceptionHandler) {
        Class<? extends Object> clazz = obj.getClass();
        String key = clazz.getCanonicalName() + "." + methodName;
        Method method = methodCache.get(key);
        if (method == null) {
            method = RpcUtils.findMethod(clazz, methodName, args);
            if (method == null) {
                throw new RpcException("method not exist method:" + methodName);
            }
            methodCache.put(key, method);
        }
        return RpcUtils.invoke(method, obj, args,exceptionHandler);
    }

    public static Method findMethod(Class clazz, String name, Object[] args) {
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    /**
     * 调用对象的方法执行
     * @param method
     * @param obj
     * @param args
     * @param exceptionHandler
     * @return
     */
    public static Object invoke(Method method, Object obj, Object[] args,RpcExceptionHandler exceptionHandler) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException e) {
            throw new RpcException("invoke IllegalAccess request access error");
        } catch (IllegalArgumentException e) {
            throw new RpcException("invoke IllegalArgument request param wrong");
        } catch (InvocationTargetException e) {
            if(e.getCause()!=null){
                exceptionHandler.handleException(null, null, e.getCause());
            }else{
                exceptionHandler.handleException(null, null, e);
            }
            throw new RpcException("rpc invoke target error");
        }
    }

    public static void handleException(RpcExceptionHandler rpcExceptionHandler,RpcObject rpc,RemoteCall call,Exception e){
        if(rpcExceptionHandler!=null){
            rpcExceptionHandler.handleException(rpc,call,e);
        }else{
            logger.error("exceptionHandler null exception message:"+e.getMessage());
        }
    }

    public static byte[] intToBytes(int iSource) {
        byte[] bLocalArr = new byte[4];
        for (int i=0;i<bLocalArr.length; i++) {
            bLocalArr[i] = (byte) (iSource >> 8*(3-i) & 0xFF);
        }
        return bLocalArr;
    }

    public static int bytesToInt(byte[] bRefArr) {
        int iOutcome = 0;
        byte bLoop;
        for (int i=0; i<bRefArr.length; i++) {
            bLoop = bRefArr[i];
            iOutcome += (bLoop & 0xFF) << (8 * (3-i));
        }
        return iOutcome;
    }

    public static byte[] longToBytes(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 7; i>-1; i--) {
            b[i] = new Long(temp & 0xff).byteValue();
            temp = temp >> 8;
        }
        return b;
    }

    public static long bytesToLong(byte[] b) {
        long s = 0;
        long s0 = b[0] & 0xff;
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;
        s6 <<= 8;
        s5 <<= 16;
        s4 <<= 24;
        s3 <<= 8 * 4;
        s2 <<= 8 * 5;
        s1 <<= 8 * 6;
        s0 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    public static String genAddressString(String prefix,InetSocketAddress address){
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(address.getAddress().getHostAddress());
        sb.append(":");
        sb.append(address.getPort());
        return sb.toString();
    }
}
