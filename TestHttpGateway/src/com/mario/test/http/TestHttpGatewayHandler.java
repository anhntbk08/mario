package com.mario.test.http;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.MapTuple;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuObjectRO;

public class TestHttpGatewayHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		PuObject data = (PuObject) message.getData();
		data.getString("command");
		return message.getData() == null || ((PuObjectRO) message.getData()).size() == 0
				? PuObject.fromObject(new MapTuple<>("status", 0)) : message.getData();
	}
}
