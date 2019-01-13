package com.flayway.fl.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HeaderDataValueInfo {
	
	private Log log = LogFactory.getLog(HeaderDataValueInfo.class);
	private Map<String, Object> map = new HashMap<String, Object>();
	
	public Object get(String key) {
		return map.get(key);
	}
	
	public boolean put(String key, Object val) {
		if(map.containsKey(key)) {
			log.info("key已存在");
			return false;
		}
		map.put(key, val);
		return true;
	}
	
	public void cpyMap(Map<String, Object> map) {
		for(Entry<String, Object> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	
}
