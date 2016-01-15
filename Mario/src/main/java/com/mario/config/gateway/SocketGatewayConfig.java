package com.mario.config.gateway;

import com.mario.entity.message.transcoder.binary.BinaryMessageSerializer;
import com.mario.entity.message.transcoder.socket.SocketMessageDeserializer;
import com.mario.gateway.socket.SocketProtocol;

public class SocketGatewayConfig extends GatewayConfig {

	private String host = null;
	private int port = -1;
	private boolean useLengthPrepender = true;
	private SocketProtocol protocol = SocketProtocol.TCP;

	{
		this.setType(GatewayType.SOCKET);
		this.setDeserializerClassName(SocketMessageDeserializer.class.getName());
		this.setSerializerClassName(BinaryMessageSerializer.class.getName());
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

	public SocketProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(SocketProtocol protocol) {
		this.protocol = protocol;
	}

	public boolean isUseLengthPrepender() {
		return useLengthPrepender;
	}

	public void setUseLengthPrepender(boolean useLengthPrepender) {
		this.useLengthPrepender = useLengthPrepender;
	}

}
