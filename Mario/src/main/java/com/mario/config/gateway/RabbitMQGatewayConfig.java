package com.mario.config.gateway;

import com.mario.entity.message.transcoder.binary.BinaryMessageSerializer;
import com.mario.entity.message.transcoder.rabbitmq.RabbitMQMessageDeserializer;
import com.nhb.messaging.rabbit.RabbitMQQueueConfig;

public class RabbitMQGatewayConfig extends GatewayConfig {

	{
		this.setType(GatewayType.RABBITMQ);
		this.setDeserializerClassName(RabbitMQMessageDeserializer.class.getName());
		this.setSerializerClassName(BinaryMessageSerializer.class.getName());
	}

	private RabbitMQQueueConfig queueConfig;
	private String serverWrapperName;

	public RabbitMQQueueConfig getQueueConfig() {
		return queueConfig;
	}

	public void setQueueConfig(RabbitMQQueueConfig queueConfig) {
		this.queueConfig = queueConfig;
	}

	public String getServerWrapperName() {
		return serverWrapperName;
	}

	public void setServerWrapperName(String serverWrapperName) {
		this.serverWrapperName = serverWrapperName;
	}

}
