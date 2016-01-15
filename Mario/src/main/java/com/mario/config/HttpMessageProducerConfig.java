package com.mario.config;

import com.mario.config.gateway.GatewayType;
import com.nhb.messaging.http.HttpMethod;

public class HttpMessageProducerConfig extends MessageProducerConfig {

	{
		setGatewayType(GatewayType.HTTP);
	}

	private String endpoint;
	private boolean async = false;
	private boolean usingMultipath = true;
	private HttpMethod httpMethod = HttpMethod.GET;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isUsingMultipath() {
		return usingMultipath;
	}

	public void setUsingMultipath(boolean usingMultipath) {
		this.usingMultipath = usingMultipath;
	}

}
