package com.mario.gateway.kafka;

import com.mario.config.gateway.KafkaGatewayConfig;
import com.mario.entity.message.Message;
import com.mario.gateway.AbstractGateway;
import com.nhb.common.data.PuElement;
import com.nhb.messaging.kafka.consumer.KafkaMessageConsumer;

public class KafkaGateway extends AbstractGateway<KafkaGatewayConfig> {

	private KafkaMessageConsumer consumer;

	@Override
	protected void _init() {
		this.consumer = new KafkaMessageConsumer(this.getConfig().getSource());
		
	}

	@Override
	protected void _start() throws Exception {
		this.consumer.start();
	}

	@Override
	protected void _stop() throws Exception {
		if (this.consumer != null) {
			this.consumer.stop();
		}
	}

	@Override
	public void onHandleComplete(Message message, PuElement result) {

	}

	@Override
	public void onHandleError(Message message, Throwable exception) {

	}

}
