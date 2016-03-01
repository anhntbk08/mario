package com.nhb.messaging.kafka.consumer;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;

import com.nhb.common.data.PuElement;
import com.nhb.eventdriven.impl.BaseEventDispatcher;
import com.nhb.messaging.kafka.event.KafkaEvent;
import com.nhb.messaging.kafka.serialization.KafkaPuElementDeserializer;

public class KafkaMessageConsumer extends BaseEventDispatcher {

	private final AtomicBoolean closer = new AtomicBoolean(false);
	private List<String> topics;
	private KafkaConsumer<byte[], PuElement> consumer;

	private int pollTimeout = 100;
	private Thread poolingThead;

	private Properties properties;

	public KafkaMessageConsumer(Properties properties, List<String> topics, int pollTimeout) {
		if (properties == null) {
			throw new IllegalArgumentException("Properties for kafka message consumer cannot be null");
		}
		if (topics == null || topics.size() == 0) {
			throw new IllegalArgumentException("Topics cannot be empty");
		}

		properties.put("key.deserializer", ByteArrayDeserializer.class.getName());
		properties.put("value.deserializer", KafkaPuElementDeserializer.class.getName());

		this.properties = properties;

		this.consumer = new KafkaConsumer<>(properties);
		this.topics = topics;
		this.pollTimeout = pollTimeout;
	}

	public void start() {
		this.consumer.subscribe(this.topics);
		this.poolingThead = new Thread() {

			@Override
			public void run() {
				while (!closer.get()) {
					ConsumerRecords<byte[], PuElement> records = consumer.poll(pollTimeout);
					Iterator<ConsumerRecord<byte[], PuElement>> it = records.iterator();
					while (it.hasNext()) {
						ConsumerRecord<byte[], PuElement> record = it.next();
						KafkaEvent event = KafkaEvent.newInstance(record);
						dispatchEvent(event);
					}
				}
			}
		};
		this.poolingThead.start();
		getLogger().info("Kafka Message Consumer started successfully with properties: {");
		for (Object key : this.properties.keySet()) {
			getLogger("pureLogger").info("\t" + key + " = " + properties.getProperty((String) key));
		}
		getLogger("pureLogger").info("}");
	}

	public void stop() {
		if (this.consumer != null) {
			this.closer.set(true);
			this.consumer.wakeup();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Properties getProperties() {
		return this.properties;
	}
}
