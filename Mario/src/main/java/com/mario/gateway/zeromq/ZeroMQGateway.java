package com.mario.gateway.zeromq;

import org.zeromq.ZMQ;

import com.mario.config.gateway.ZeroMQGatewayConfig;
import com.mario.entity.message.Message;
import com.mario.gateway.AbstractGateway;
import com.mario.gateway.socket.SocketProtocol;
import com.nhb.common.data.PuElement;

public class ZeroMQGateway extends AbstractGateway<ZeroMQGatewayConfig> {

	private ZMQ.Context context;
	private ZMQ.Socket socket;

	@Override
	protected void _init() {
		this.context = ZMQ.context(1);
	}

	@Override
	protected void _start() throws Exception {
		ZeroMQGatewayConfig config = this.getConfig();

		socket = context.socket(config.getSocketType().getType());
		socket.bind(config.getProtocol().name().toLowerCase() + "://" + config.getHost()
				+ (config.getProtocol() == SocketProtocol.TCP ? (":" + config.getPort()) : ""));
	}

	@Override
	protected void _stop() throws Exception {
		socket.close();
		context.term();
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHandleError(Message message, Throwable exception) {
		// TODO Auto-generated method stub

	}

}
