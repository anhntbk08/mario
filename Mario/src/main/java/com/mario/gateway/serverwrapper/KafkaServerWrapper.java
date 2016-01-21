package com.mario.gateway.serverwrapper;

import com.mario.config.serverwrapper.KafkaServerWrapperConfig;

import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaServerWrapper extends BaseServerWrapper {

	private ConsumerConnector consumerConnector;

	@Override
	public void start() {
		KafkaServerWrapperConfig config = (KafkaServerWrapperConfig) this.getConfig();
		this.consumerConnector = consumerConnector
	}

	@Override
	public void stop() {

	}

}
