package com.nhb.messaging.kafka.producer;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.kafka.config.KafkaProducerConfig;

public class KafkaMessageProducer extends BaseLoggable implements MessageProducer<Boolean> {

	private KafkaProducerConfig config;

	public KafkaMessageProducer() {
		// do nothing
	}

	public KafkaMessageProducer(KafkaProducerConfig config) {
		this();
		this.config = config;
	}

	@Override
	public Boolean publish(PuElement data) {
		return null;
	}

	@Override
	public Boolean publish(PuElement data, String rountingKey) {
		return null;
	}

	public KafkaProducerConfig getConfig() {
		return config;
	}

	public void setConfig(KafkaProducerConfig config) {
		this.config = config;
	}

	public void start() {
		
	}
	
	public void stop() {
		
	}

}
