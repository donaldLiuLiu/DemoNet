package com.flayway.fl.packer;

import java.io.Serializable;

public interface IMessagePacker {
	public Object pack(Serializable param, MessagePackerInfo info);
	public Serializable unpack(Object param, MessagePackerInfo info);
}
