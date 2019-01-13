package com.flayway.fl.test;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.flayway.fl.resolver.IReqResolver;
import com.flayway.fl.test.vo.JsonReqVo;
import com.flayway.fl.test.vo.JsonRespVo;

public class ReqJsonResolver implements IReqResolver {
	
	private Log log = LogFactory.getLog(ReqJsonResolver.class);
	
	@Override
	public Serializable execute(Serializable param) throws Exception {
		
		JsonReqVo req = (JsonReqVo) param;
		log.info(req.getReqcode() + " : " + req.getReqmsg());
		
		JsonRespVo resp = new JsonRespVo();
		resp.setRespcode("0000");
		resp.setRespmsg("缴费卡刷卡机");
		
		return resp;
		
	}

}
