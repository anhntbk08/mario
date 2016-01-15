package com.mario.statics;

import org.zeromq.ZMQ;

public enum ZMQSocketType {

	PUB(ZMQ.PUB), SUB(ZMQ.SUB), REQ(ZMQ.REQ), REP(ZMQ.REP), PUSH(ZMQ.PUSH), PULL(ZMQ.PULL);

	private int type;

	private ZMQSocketType(int type) {
		this.type = type;
	}

	public int getType() {
		return this.type;
	}

	public static ZMQSocketType fromName(String name) {
		if (name != null) {
			for (ZMQSocketType type : values()) {
				if (type.name().equalsIgnoreCase(name.trim())) {
					return type;
				}
			}
		}
		return null;
	}
}
