package com.nhb.messaging.kafka.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.nhb.common.vo.HostAndPort;

public class ZooKeeperConfig {

	private Collection<HostAndPort> endpoints = new HashSet<>();
	private int connectionTimeout = 400;
	private int syncTime = 200;

	public String getConnectionString() {
		if (endpoints.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (HostAndPort endpoint : this.endpoints) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(endpoint.toString());
			}
			return sb.toString().trim();
		}
		return "";
	}

	public Collection<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void addEndpoint(HostAndPort... endpoints) {
		this.endpoints.addAll(Arrays.asList(endpoints));
	}

	public void addEndpoint(Collection<HostAndPort> endpoints) {
		this.endpoints.addAll(endpoints);
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(int syncTime) {
		this.syncTime = syncTime;
	}
}
