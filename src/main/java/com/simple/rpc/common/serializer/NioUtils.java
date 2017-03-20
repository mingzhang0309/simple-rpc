package com.simple.rpc.common.serializer;

import com.simple.rpc.common.RpcObject;
import com.simple.rpc.common.RpcUtils;
import com.simple.rpc.common.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * NIO以及网络utils
 * @author lindezhi
 * 2016年3月9日 上午11:36:15
 */
public class NioUtils {
	
	private static Logger logger = LoggerFactory.getLogger(NioUtils.class);
	
	public static final int RPC_PROTOCOL_HEAD_LEN = 20;
	
	public static byte[] zip(byte[] bytes){
		if(bytes!=null&&bytes.length>0){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				GZIPOutputStream gos = new GZIPOutputStream(bos);
				gos.write(bytes);
				gos.close();
				return bos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}
	
	public static byte[] unzip(byte[] bytes){
		try {
			GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buff = new byte[512];
			int read = gis.read(buff);
			while(read>0){
				bos.write(buff,0,read);
				read = gis.read(buff);
			}
			gis.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static void clearNioWriteOp(SelectionKey key){
		if(checkKey(key)){
			final int interestOps = key.interestOps();
			if ((interestOps & SelectionKey.OP_WRITE) != 0) {
				key.interestOps(interestOps & ~SelectionKey.OP_WRITE);
			}
		}
	}
	
	public static void setNioWriteOp(SelectionKey key){
		if(checkKey(key)){
	        final int interestOps = key.interestOps();
	        if ((interestOps & SelectionKey.OP_WRITE) == 0) {
	            key.interestOps(interestOps | SelectionKey.OP_WRITE);
	        }
		}
	}
	
	public static void clearNioReadOp(SelectionKey key){
		if (checkKey(key)) {
			int interestOps = key.interestOps();
			if ((interestOps & SelectionKey.OP_READ) != 0) {
				key.interestOps(interestOps & ~SelectionKey.OP_READ);
			}
		}
	}
	
	public static void setNioReadOp(SelectionKey key){
		if(checkKey(key)){
	        final int interestOps = key.interestOps();
	        if ((interestOps & SelectionKey.OP_READ) == 0) {
	        	key.interestOps(interestOps | SelectionKey.OP_READ);
	        }
		}
	}
	
	private static boolean checkKey(SelectionKey key){
        if (!key.isValid()) {
        	logger.info("valid selection key");
            return false;
        }
        logger.info("selection key ok");
        return true;
	}
	
	/**
	 * type|threadId|index|length|data
	 * @return
	 */
	public static boolean writeBuffer(ByteBuffer buffer,RpcObject object){
		if (object.getLength() > RpcUtils.MEM_1M) {
			throw new RpcException("rpc data too long "+ object.getLength());
		}
		buffer.putInt(object.getType().getType());
		buffer.putLong(object.getThreadId());
		buffer.putInt(object.getIndex());
		buffer.putInt(object.getLength());
		buffer.put(object.getData());
		return true;
	}
	
	public static void logBuffer(String clazz,String key,ByteBuffer buffer){
		logger.info(clazz+" "+key+" buff position:"+buffer.position()+" limit:"+buffer.limit()+" capacity:"+buffer.capacity());
	}
	
	public static RpcObject readBuffer(ByteBuffer buffer){
		RpcObject object = new RpcObject();
		object.setType(RpcUtils.RpcType.getByType(buffer.getInt()));
		object.setThreadId(buffer.getLong());
		object.setIndex(buffer.getInt());
		object.setLength(buffer.getInt());
		if (object.getLength() > RpcUtils.MEM_1M) {
			throw new RpcException("rpc data too long "+ object.getLength());
		}
		if(object.getLength()>0){
			byte[] buf = new byte[object.getLength()];
			buffer.get(buf, 0, buf.length);
			object.setData(buf);
		}
		return object;
	}

}
