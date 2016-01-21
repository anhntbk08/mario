package com.mario.config.gateway;

import com.nhb.messaging.kafka.config.KafkaConsumerConfig;
import com.nhb.messaging.kafka.config.ZooKeeperConfig;

public class KafkaGatewayConfig extends GatewayConfig {

	{
		this.setType(GatewayType.KAFKA);
	}

	private final KafkaConsumerConfig source = new KafkaConsumerConfig();

	public int getAutoCommitInterval() {
		return source.getAutoCommitInterval();
	}

	public void setAutoCommitInterval(int autoCommitInterval) {
		source.setAutoCommitInterval(autoCommitInterval);
	}

	public String getTopic() {
		return source.getTopic();
	}

	public void setTopic(String topic) {
		source.setTopic(topic);
	}

	public ZooKeeperConfig getZooKeeperConfig() {
		return source.getZooKeeperConfig();
	}

	public void setZooKeeperConfig(ZooKeeperConfig zooKeeperConfig) {
		source.setZooKeeperConfig(zooKeeperConfig);
	}

	public String getGroupId() {
		return source.getGroupId();
	}

	public void setGroupId(String groupId) {
		source.setGroupId(groupId);
	}

	public int getTimeout() {
		return source.getTimeout();
	}

	public void setTimeout(int timeout) {
		source.setTimeout(timeout);
	}

	public KafkaConsumerConfig getSource() {
		return this.source;
	}
}
