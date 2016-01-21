package com.mario.config.serverwrapper;

import com.nhb.messaging.kafka.config.ZooKeeperConfig;

public class KafkaServerWrapperConfig extends ServerWrapperConfig {

	{
		this.setType(ServerWrapperType.ZOOKEEPER);
	}

	private ZooKeeperConfig zooKeeperConfig;

	public ZooKeeperConfig getZooKeeperConfig() {
		return this.zooKeeperConfig;
	}
}
