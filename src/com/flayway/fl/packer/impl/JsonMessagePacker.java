package com.flayway.fl.packer.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flayway.fl.packer.IMessagePacker;
import com.flayway.fl.packer.MessagePackerInfo;

/*ObjectMapper的解析特点（创建VO时，有助于引导属性的创建方式）
 * {"name":"123","ii":1,"list":["1","2"],"map":{"1234":"123","1235":"123","123":"123"}}
 * String name 
 * int ii 
 * List<String> list
 * Map<String,Object> map
 * Vo vo (与map一致)
 */
public class JsonMessagePacker implements IMessagePacker {
	
	
	@Override
	public Object pack(Serializable param, MessagePackerInfo info) {
		//param  -->   json字符串     -->   字节数组
		ObjectMapper objMapper = new ObjectMapper();
		byte[] by = null;
		try {
			by = objMapper.writeValueAsBytes(param);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("打包异常", e);
		}
		return by;
	}
	
	@Override
	public Serializable unpack(Object param, MessagePackerInfo info) {
		//字节数组   -->  json字符串    -->  serializable
		ObjectMapper objMapper = new ObjectMapper();
		Serializable ret = null;
		if(param instanceof byte[]) {
			try {
				ret = (Serializable) objMapper.readValue((byte[]) param, info.getClz());
			} catch (Exception e) {
				throw new RuntimeException("解包异常," + e.getMessage(), e);
			}
		} else {
			throw new RuntimeException("数据包格式异常");
		}
		return ret;
	}
	
	public static void main(String argv[]) throws Exception{
		
		
	}
}
