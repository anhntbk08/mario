package com.mario.config;

import java.util.ArrayList;
import java.util.List;

import com.nhb.common.vo.HostAndPort;

public class RedisConfig extends MarioBaseConfig {

	public static enum RedisType {
		SINGLE, MASTER_SLAVE, SENTINEL, CLUSTER;

		public static RedisType fromName(String name) {
			if (name != null) {
				for (RedisType type : values()) {
					if (type.name().equalsIgnoreCase(name.trim())) {
						return type;
					}
				}
			}
			return null;
		}
	}

	private List<HostAndPort> endpoints;

	private RedisType redisType = RedisType.SINGLE;
	private String password;
	private int poolSize;
	private int timeout = 10000;
	private int scanInterval = 2000;
	private String loadBalancer = null;

	private String masterName;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPoolSize() {
		if (poolSize == 0) {
			return 4;
		}
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public List<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void addEndpoint(HostAndPort hostAndPort) {
		if (this.endpoints == null) {
			this.endpoints = new ArrayList<>();
		}
		this.endpoints.add(hostAndPort);
	}

	public void addEndpoint(String host, int port) {
		this.addEndpoint(new HostAndPort(host, port));
	}

	public HostAndPort getFirstEndpoint() {
		return this.endpoints != null ? this.endpoints.get(0) : null;
	}

	public RedisType getRedisType() {
		return this.redisType;
	}

	public void setRedisType(RedisType type) {
		this.redisType = type;
	}

	public void setRedisType(String typeName) {
		this.setRedisType(RedisType.fromName(typeName));
	}

	public int getScanInterval() {
		return scanInterval;
	}

	public void setScanInterval(int scanInterval) {
		this.scanInterval = scanInterval;
	}

	public String getLoadBalancer() {
		return loadBalancer;
	}

	public void setLoadBalancer(String loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	public String getMasterName() {
		return masterName;
	}

	public void setMasterName(String masterName) {
		this.masterName = masterName;
	}

}
