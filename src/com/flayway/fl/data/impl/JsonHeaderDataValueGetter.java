package com.flayway.fl.data.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flayway.fl.data.HeaderDataValueInfo;
import com.flayway.fl.data.IHeaderDataValueGetter;

public class JsonHeaderDataValueGetter implements IHeaderDataValueGetter {

	@Override
	public Map<String, Object> getValues(Object param, HeaderDataValueInfo info) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		String key = (String) info.get(TRANSCODE);
		Class<?> clz = (Class<?>) info.get(REQCODEVO);
		
		ObjectMapper objMapper = new ObjectMapper();
		Serializable ret = null;
		if(param instanceof byte[]) {
			try {
				ret = (Serializable) objMapper.readValue((byte[]) param, clz);
			} catch (Exception e) {
				throw new RuntimeException("解包异常," + e.getMessage(), e);
			}
		} else {
			throw new RuntimeException("数据包格式异常");
		}
		//ret 的  key 属性名
		Class<? extends Serializable> clx = ret.getClass();
		try {
			Field fl = clx.getDeclaredField(key);
			fl.setAccessible(true);
			retMap.put(TRANSCODE, fl.get(ret));
		} catch (Exception e) {
			throw new RuntimeException("取请求码失败");
		}
		
		return retMap;
	}
	
	

}
