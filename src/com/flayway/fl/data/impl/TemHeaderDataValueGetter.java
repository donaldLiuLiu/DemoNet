package com.flayway.fl.data.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.flayway.fl.data.HeaderDataValueInfo;
import com.flayway.fl.data.IHeaderDataValueGetter;

public class TemHeaderDataValueGetter implements IHeaderDataValueGetter {

	@Override
	public Map<String, Object> getValues(Object param, HeaderDataValueInfo info) {
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		int start = Integer.parseInt((String) info.get(START));
		int end = Integer.parseInt((String) info.get(END));
		
		byte[] by = new byte[end-start+1];
		System.arraycopy(param, 0, by, 0, end-start+1);
		
		try {
			retMap.put(TRANSCODE, new String(by, (String) info.get(ENCODING)));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
		return retMap;
	}
}
