package com.mario.entity.message.impl;

import com.mario.entity.message.RabbitMQDeliveredMessage;
import com.mario.entity.message.RabbitMQMessage;

public class BaseRabbitMQMessage extends BaseMessage implements RabbitMQMessage {

	private RabbitMQDeliveredMessage deliveredMessage;

	@Override
	public void clear() {
		super.clear();
		this.deliveredMessage = null;
	}

	@Override
	public RabbitMQDeliveredMessage getDeliveredMessage() {
		return deliveredMessage;
	}

	@Override
	public void setDeliveredMessage(RabbitMQDeliveredMessage deliveredMessage) {
		this.deliveredMessage = deliveredMessage;
	}
}
