package com.nhb.common.db.mongodb.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nhb.common.vo.HostAndPort;

public class MongoDBConfig {

	private String name;
	private List<MongoDBCredentialConfig> credentialConfigs = new ArrayList<MongoDBCredentialConfig>();
	private List<HostAndPort> endpoints = new ArrayList<HostAndPort>();

	public MongoDBConfig() {
		// do nothing
	}

	public MongoDBConfig(String name) {
		this();
		this.setName(name);
	}

	public MongoDBConfig(String name, HostAndPort... networkConfigs) {
		this(name);
		this.getEndpoints().addAll(Arrays.asList(networkConfigs));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void addEndpoint(String host, int port) {
		this.addEndpoint(new HostAndPort(host, port));
	}

	public void addEndpoint(HostAndPort config) {
		if (config != null) {
			this.getEndpoints().add(config);
		}
	}

	public void addEndpoints(HostAndPort... configs) {
		if (configs != null) {
			this.getEndpoints().addAll(Arrays.asList(configs));
		}
	}

	public List<MongoDBCredentialConfig> getCredentialConfigs() {
		return credentialConfigs;
	}

	public void addCredentialConfig(MongoDBCredentialConfig config) {
		if (config != null) {
			this.getCredentialConfigs().add(config);
		}
	}

	public void addCredentialConfigs(MongoDBCredentialConfig... configs) {
		if (configs != null) {
			this.getCredentialConfigs().addAll(Arrays.asList(configs));
		}
	}
}
