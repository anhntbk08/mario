package com.mario.entity.message.transcoder.http;

import com.mario.entity.message.transcoder.MessageEncoder;

public abstract class HttpMessageSerializer implements MessageEncoder {

	@Override
	public final Object encode(Object data) throws Exception {
		return this.encodeHttpResponse(data);
	}

	protected abstract String encodeHttpResponse(Object data) throws Exception;

}
