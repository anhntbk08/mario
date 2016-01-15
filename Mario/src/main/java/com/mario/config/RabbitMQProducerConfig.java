package com.mario.config;

import com.mario.config.gateway.GatewayType;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;

public class RabbitMQProducerConfig extends MessageProducerConfig {

	{
		this.setGatewayType(GatewayType.RABBITMQ);
	}

	private int timeout;
	private RabbitMQQueueConfig queueConfig;
	private String connectionName;

	public RabbitMQQueueConfig getQueueConfig() {
		return queueConfig;
	}

	public void setQueueConfig(RabbitMQQueueConfig queueConfig) {
		this.queueConfig = queueConfig;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
}
