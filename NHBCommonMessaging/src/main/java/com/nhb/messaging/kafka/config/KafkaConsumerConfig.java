package com.nhb.messaging.kafka.config;

import java.util.Properties;

public class KafkaConsumerConfig {

	private ZooKeeperConfig zooKeeperConfig = new ZooKeeperConfig();
	private String groupId;
	private int timeout; // ms
	private int autoCommitInterval = 1000;
	private String topic;

	public ZooKeeperConfig getZooKeeperConfig() {
		return zooKeeperConfig;
	}

	public void setZooKeeperConfig(ZooKeeperConfig zooKeeperConfig) {
		this.zooKeeperConfig = zooKeeperConfig;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getAutoCommitInterval() {
		return autoCommitInterval;
	}

	public void setAutoCommitInterval(int autoCommitInterval) {
		this.autoCommitInterval = autoCommitInterval;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Properties toProperties() {
		Properties props = new Properties();
		props.put("group.id", this.groupId);
		props.put("zookeeper.connect", this.zooKeeperConfig.getConnectionString());
		props.put("zookeeper.session.timeout.ms", String.valueOf(this.zooKeeperConfig.getConnectionTimeout()));
		props.put("zookeeper.sync.time.ms", String.valueOf(this.zooKeeperConfig.getSyncTime()));
		props.put("auto.commit.interval.ms", String.valueOf(this.autoCommitInterval));
		return props;
	}
}
