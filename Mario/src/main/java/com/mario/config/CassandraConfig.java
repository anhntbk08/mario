package com.mario.config;

import java.util.Collection;
import java.util.HashSet;

import com.nhb.common.db.cassandra.CassandraDatasourceConfig;
import com.nhb.common.vo.HostAndPort;

public class CassandraConfig extends MarioBaseConfig implements CassandraDatasourceConfig {

	private Collection<HostAndPort> endpoints = new HashSet<>();
	private String keyspace;

	@Override
	public Collection<HostAndPort> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(Collection<HostAndPort> endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}
}
