package com.flayway.fl.test.vo;

import java.io.Serializable;
import java.util.List;

public class TemReqVo implements Serializable{
	private String reqcode;
	private String reqmsg;
	
	private String listlength;
	private List<ReqVoVo> vos;
	
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
	public String getListlength() {
		return listlength;
	}
	public void setListlength(String listlength) {
		this.listlength = listlength;
	}
	public List<ReqVoVo> getVos() {
		return vos;
	}
	public void setVos(List<ReqVoVo> vos) {
		this.vos = vos;
	}
	
}
