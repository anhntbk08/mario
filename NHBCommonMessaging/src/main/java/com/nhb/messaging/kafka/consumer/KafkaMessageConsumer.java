package com.nhb.messaging.kafka.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nhb.common.data.PuElement;
import com.nhb.eventdriven.impl.BaseEventDispatcher;
import com.nhb.messaging.kafka.config.KafkaConsumerConfig;
import com.nhb.messaging.kafka.serialization.KafkaMsgpackDecoder;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;

public class KafkaMessageConsumer extends BaseEventDispatcher {

	private KafkaConsumerConfig config;

	private ConsumerConnector consumerConnector;

	private Map<String, List<KafkaStream<String, PuElement>>> messageStreams;

	public KafkaMessageConsumer(KafkaConsumerConfig config) {
		this.setConfig(config);

		this.consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(this.config.toProperties()));

		Map<String, Integer> topicCountMap = new HashMap<>();

		this.messageStreams = this.consumerConnector.createMessageStreams(topicCountMap, new StringDecoder(null),
				new KafkaMsgpackDecoder());
	}

	public void start() {
		for (KafkaStream<String, PuElement> stream : this.messageStreams.get(this.config.getTopic())) {
			MessageAndMetadata<String, PuElement> it = stream.iterator().next();
			
		}
	}

	public void stop() {
		if (this.consumerConnector != null) {
			this.consumerConnector.shutdown();
		}
	}

	public KafkaConsumerConfig getConfig() {
		return config;
	}

	public void setConfig(KafkaConsumerConfig config) {
		this.config = config;
	}
}
