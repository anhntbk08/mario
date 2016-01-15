package com.mario.test.http;

import com.mario.entity.impl.BaseMessageHandler;
import com.mario.entity.message.Message;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;

public class TestHttpGatewayHandler extends BaseMessageHandler {

	@Override
	public PuElement handle(Message message) {
		return new PuValue(message.getData().toJSON());
		// HttpAsyncMessageProducer forwarder = getApi().getProducer("maxpay");
		//
		// MessageHandleCallback messageHandleCallback = message.getCallback();
		//
		// final Message cloneMessage = (message instanceof CloneableMessage) ?
		// ((CloneableMessage) message).makeClone()
		// : null;
		// forwarder.publish(message.getData()).setCallback(new
		// Callback<HttpResponse>() {
		//
		// @Override
		// public void apply(HttpResponse result) {
		// if (messageHandleCallback != null) {
		// messageHandleCallback.onHandleComplete(cloneMessage,
		// HttpClientHelper.handleResponse(result));
		// }
		// }
		// });
		//
		// return PuNull.IGNORE_ME;
	}
}
