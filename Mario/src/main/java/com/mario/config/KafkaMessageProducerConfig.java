package com.mario.config;

import com.mario.config.gateway.GatewayType;

public class KafkaMessageProducerConfig extends MessageProducerConfig {

	{
		this.setGatewayType(GatewayType.KAFKA);
	}

	private String configFile;
	private String topic;

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
