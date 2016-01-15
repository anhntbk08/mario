package com.mario.entity.message.impl;

import com.mario.config.gateway.GatewayType;
import com.mario.entity.MessageHandleCallback;
import com.mario.entity.message.MessageForwardable;
import com.nhb.common.data.PuElement;
import com.nhb.messaging.MessageForwarder;

public class BaseMessage implements MessageForwardable {

	private PuElement data;
	private String gatewayName;
	private GatewayType gatewayType;
	private MessageForwarder forwarder;
	private MessageHandleCallback callback;

	@Override
	public void clear() {
		this.data = null;
	}

	@Override
	public PuElement getData() {
		return data;
	}

	@Override
	public void setData(PuElement data) {
		this.data = data;
	}

	@Override
	public MessageForwarder getForwarder() {
		return forwarder;
	}

	@Override
	public void setForwarder(MessageForwarder forwarder) {
		this.forwarder = forwarder;
	}

	@Override
	public String getGatewayName() {
		return gatewayName;
	}

	@Override
	public void setGatewayName(String gatewayName) {
		this.gatewayName = gatewayName;
	}

	@Override
	public GatewayType getGatewayType() {
		return gatewayType;
	}

	@Override
	public void setGatewayType(GatewayType gatewayType) {
		this.gatewayType = gatewayType;
	}

	public MessageHandleCallback getCallback() {
		return callback;
	}

	public void setCallback(MessageHandleCallback callback) {
		this.callback = callback;
	}
}
