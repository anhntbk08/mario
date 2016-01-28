package com.nhb.messaging.kafka.producer;

import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.utils.Converter;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.kafka.serialization.KafkaPuElementSerializer;

public class KafkaMessageProducer extends BaseLoggable implements MessageProducer<byte[]> {

	private KafkaProducer<byte[], PuElement> producer;
	private String defaultTopic;

	public KafkaMessageProducer(Properties properties, String defaultTopic) {
		if (properties == null) {
			throw new RuntimeException("Properties for kafka producer cannot be null");
		}

		properties.put("key.serializer", ByteArraySerializer.class.getName());
		properties.put("value.serializer", KafkaPuElementSerializer.class.getName());

		this.producer = new KafkaProducer<>(properties);
		this.defaultTopic = defaultTopic;
	}

	public void stop() {
		if (this.producer != null) {
			this.producer.close();
		}
	}

	@Override
	public byte[] publish(PuElement data) {
		byte[] key = Converter.uuidToBytes(UUID.randomUUID());
		this.producer.send(new ProducerRecord<byte[], PuElement>(this.defaultTopic, key, data));
		return key;
	}

	@Override
	public byte[] publish(PuElement data, String topic) {
		if (topic == null) {
			throw new IllegalArgumentException("topic cannot be null");
		}
		byte[] key = Converter.uuidToBytes(UUID.randomUUID());
		this.producer.send(new ProducerRecord<byte[], PuElement>(topic, key, data));
		return key;
	}

}
