package com.mario.config.serverwrapper;

public class HttpServerWrapperConfig extends ServerWrapperConfig {

	private String host;
	private int port;
	private int options = 0;
	private int sessionTimeout = 1200; // s

	{
		this.setType(ServerWrapperType.HTTP);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getOptions() {
		return options;
	}

	public void setOptions(int options) {
		this.options = options;
	}

	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

}
