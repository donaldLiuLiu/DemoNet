package com.flayway.fl.test.vo;

import java.io.Serializable;

public class JsonRespVo implements Serializable{
	private String respcode;
	private String respmsg;
	public String getRespcode() {
		return respcode;
	}
	public void setRespcode(String respcode) {
		this.respcode = respcode;
	}
	public String getRespmsg() {
		return respmsg;
	}
	public void setRespmsg(String respmsg) {
		this.respmsg = respmsg;
	}
}
