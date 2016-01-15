package com.mario.gateway;

import com.nhb.eventdriven.impl.AbstractEvent;

public class GatewayEvent extends AbstractEvent {

	public static final String BEFORE_START = "beforeStart";
	public static final String STARTED = "started";

	public static GatewayEvent createBeforeStartEvent() {
		return new GatewayEvent(BEFORE_START);
	}

	public static GatewayEvent createStartedEvent() {
		return new GatewayEvent(STARTED);
	}

	public GatewayEvent(String type) {
		this.setType(type);
	}
}
