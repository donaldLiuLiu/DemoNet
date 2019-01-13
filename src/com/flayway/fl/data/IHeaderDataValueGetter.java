package com.flayway.fl.data;

import java.util.Map;

/*
 * 请求码获取接口
 */
public interface IHeaderDataValueGetter {
	public static final String TRANSCODE = "trans-code";
	public static final String REQCODEVO = "req-code-vo";
	public static final String RESPCODE = "resp-code";
	public static final String START = "tem-start";
	public static final String END = "tem-end";
	public static final String DELIM = "delim";
	public static final String ENCODING = "encoding";
	public Map<String,Object> getValues(Object param, HeaderDataValueInfo info);
}
