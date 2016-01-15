package com.mario.gateway.socket;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.nhb.eventdriven.EventDispatcher;

public interface SocketSession extends EventDispatcher {

	String getId();

	InetSocketAddress getRemoteAddress();

	void send(Object obj);

	boolean isActive();

	void close() throws IOException;

}
