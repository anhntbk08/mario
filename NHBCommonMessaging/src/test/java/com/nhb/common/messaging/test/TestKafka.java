package com.nhb.common.messaging.test;

import com.nhb.common.BaseLoggable;
import com.nhb.common.utils.Initializer;
import com.nhb.common.vo.HostAndPort;
import com.nhb.messaging.kafka.config.KafkaConsumerConfig;
import com.nhb.messaging.kafka.config.KafkaProducerConfig;
import com.nhb.messaging.kafka.config.ZooKeeperConfig;
import com.nhb.messaging.kafka.consumer.KafkaMessageConsumer;
import com.nhb.messaging.kafka.producer.KafkaMessageProducer;
import com.nhb.messaging.kafka.serialization.KafkaMsgpackDecoder;
import com.nhb.messaging.kafka.serialization.KafkaMsgpackEncoder;

public class TestKafka extends BaseLoggable {

	public static void main(String[] args) {

		Initializer.bootstrap(TestKafka.class);

		final TestKafka test = new TestKafka();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				test.stop();
			}
		});

		test.start();
	}

	private KafkaMessageProducer kafkaMessageProducer;
	private KafkaMessageConsumer kafkaMessageConsumer;

	private TestKafka() {
		ZooKeeperConfig zooKeeperConfig = new ZooKeeperConfig();
		zooKeeperConfig.addEndpoint(new HostAndPort(2181));
		zooKeeperConfig.setConnectionTimeout(6000);

		KafkaConsumerConfig gatewayConfig = new KafkaConsumerConfig();
		gatewayConfig.setGroupId("nhb-kafka-test-group");
		gatewayConfig.setTimeout(6000);
		gatewayConfig.setZooKeeperConfig(zooKeeperConfig);

		KafkaProducerConfig producerConfig = new KafkaProducerConfig();
		producerConfig.addBrokers(new HostAndPort(9092));
		producerConfig.setSerializerClass(KafkaMsgpackEncoder.class.getName());
		producerConfig.setDeserializerClass(KafkaMsgpackDecoder.class.getName());

		this.kafkaMessageConsumer = new KafkaMessageConsumer(gatewayConfig);
		this.kafkaMessageProducer = new KafkaMessageProducer(producerConfig);
	}

	private void start() {
		this.kafkaMessageConsumer.start();
		this.kafkaMessageProducer.start();
	}

	private void stop() {
		this.kafkaMessageProducer.stop();
		this.kafkaMessageConsumer.stop();
	}
}
