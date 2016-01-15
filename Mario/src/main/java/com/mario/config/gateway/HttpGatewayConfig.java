package com.mario.config.gateway;

import java.util.HashMap;
import java.util.Map;

import com.mario.entity.message.transcoder.http.DefaultHttpMessageDeserializer;
import com.mario.entity.message.transcoder.http.DefaultHttpMessageSerializer;

public class HttpGatewayConfig extends GatewayConfig {

	private String path = "/*";
	private String encoding = "utf-8";
	private String contentType = null; // "text/plain";
	private boolean useMultipath = false;
	private boolean async = false;

	private final Map<String, String> headers = new HashMap<>();

	{
		this.setType(GatewayType.HTTP);
		this.setDeserializerClassName(DefaultHttpMessageDeserializer.class.getName());
		this.setSerializerClassName(DefaultHttpMessageSerializer.class.getName());
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public boolean isUseMultipath() {
		return useMultipath;
	}

	public void setUseMultipath(boolean useMultipath) {
		this.useMultipath = useMultipath;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
}
