package com.flayway.fl.resolver;

import java.io.Serializable;

public interface IReqResolver {
	public Serializable execute(Serializable param) throws Exception;
}
