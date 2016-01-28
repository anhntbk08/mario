package com.mario.config.gateway;

import java.util.ArrayList;
import java.util.List;

import com.mario.entity.message.transcoder.kafka.KafkaDeserializer;

public class KafkaGatewayConfig extends GatewayConfig {

	{
		this.setType(GatewayType.KAFKA);
		this.setDeserializerClassName(KafkaDeserializer.class.getName());
	}

	private String configFile;
	private int pollTimeout = 100;
	private final List<String> topics = new ArrayList<>();

	public String getConfigFile() {
		return this.configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public List<String> getTopics() {
		return topics;
	}

	public int getPollTimeout() {
		return pollTimeout;
	}

	public void setPollTimeout(int pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

}
