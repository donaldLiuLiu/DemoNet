package com.flayway.fl.test.vo;

import java.io.Serializable;

public class JsonReqVo implements Serializable{
	private String reqcode;
	private String reqmsg;
	public String getReqcode() {
		return reqcode;
	}
	public void setReqcode(String reqcode) {
		this.reqcode = reqcode;
	}
	public String getReqmsg() {
		return reqmsg;
	}
	public void setReqmsg(String reqmsg) {
		this.reqmsg = reqmsg;
	}
}
