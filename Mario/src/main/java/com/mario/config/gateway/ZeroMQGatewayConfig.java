package com.mario.config.gateway;

import com.mario.gateway.socket.SocketProtocol;
import com.mario.statics.ZMQSocketType;

public class ZeroMQGatewayConfig extends GatewayConfig {

	private ZMQSocketType socketType;
	private SocketProtocol protocol;
	private String host = "*";
	private int port = 5555;

	public SocketProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(SocketProtocol protocol) {
		if (protocol == SocketProtocol.UDT || protocol == SocketProtocol.UDP) {
			throw new RuntimeException(protocol + " is invalid, ZeroMQ doesn't support for that");
		}
		this.protocol = protocol;
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

	public ZMQSocketType getSocketType() {
		return socketType;
	}

	public void setSocketType(ZMQSocketType socketType) {
		this.socketType = socketType;
	}

}
