package com.nhb.messaging.kafka.config;

public enum KafkaProducerType {

	SYNC, ASYNC;

	public static final KafkaProducerType fromName(String name) {
		if (name != null) {
			for (KafkaProducerType type : values()) {
				if (type.name().equalsIgnoreCase(name.trim())) {
					return type;
				}
			}
		}
		return null;
	}
}
