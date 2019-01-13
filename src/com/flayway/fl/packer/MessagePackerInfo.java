package com.flayway.fl.packer;

import java.util.List;
import java.util.Map;

public class MessagePackerInfo {
	private String defaultEncoding;
	private Class<?> clz;
	private List<MessagePackerItem> items;
	
	public Class<?> getClz() {
		return clz;
	}
	public void setClz(Class<?> clz) {
		this.clz = clz;
	}
	public List<MessagePackerItem> getItems() {
		return items;
	}
	public void setItems(List<MessagePackerItem> items) {
		this.items = items;
	}
	public String getDefaultEncoding() {
		return defaultEncoding;
	}
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}
	
}
