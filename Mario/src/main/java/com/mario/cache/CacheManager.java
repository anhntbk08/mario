package com.mario.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mario.cache.hazelcast.HazelcastInitializer;
import com.mario.config.HazelcastConfig;
import com.mario.config.RedisConfig;
import com.mario.extension.ExtensionManager;
import com.nhb.common.BaseLoggable;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.common.vo.HostAndPort;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

public class CacheManager extends BaseLoggable {

	private Map<String, HazelcastInstance> hazelcastInstances;
	private Map<String, JedisService> jedisServices;

	private ExtensionManager extensionManager;

	private final Map<String, HazelcastConfig> lazyInitConfigs = new ConcurrentHashMap<>();

	public CacheManager(ExtensionManager extMan) {
		this.hazelcastInstances = new HashMap<>();
		this.jedisServices = new HashMap<>();
		this.extensionManager = extMan;
	}

	public void stop() {
		for (HazelcastInstance instance : this.hazelcastInstances.values()) {
			try {
				instance.shutdown();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (JedisService jedisService : this.jedisServices.values()) {
			try {
				jedisService.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	private JedisService createJedisService(RedisConfig config) {
		JedisService jedisService = new JedisService();
		switch (config.getRedisType()) {
		case SINGLE:
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(config.getPoolSize());
			JedisPool pool = new JedisPool(poolConfig, config.getFirstEndpoint().getHost(),
					config.getFirstEndpoint().getPort(), config.getTimeout(), config.getPassword());
			jedisService.setPool(pool);
			break;
		case CLUSTER:
			Set<redis.clients.jedis.HostAndPort> jedisClusterNodes = new HashSet<redis.clients.jedis.HostAndPort>();
			for (HostAndPort hnp : config.getEndpoints()) {
				jedisClusterNodes.add(new redis.clients.jedis.HostAndPort(hnp.getHost(), hnp.getPort()));
			}
			JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes);
			jedisService.setCluster(jedisCluster);
			break;
		case SENTINEL:
			Set<String> sentinels = new HashSet<>();
			for (HostAndPort hnp : config.getEndpoints()) {
				sentinels.add(hnp.toString());
			}
			JedisSentinelPool sentinelPool = new JedisSentinelPool(config.getMasterName(), sentinels);
			jedisService.setSentinel(sentinelPool);
			break;
		case MASTER_SLAVE:
			break;
		default:
			break;
		}
		return jedisService;
	}

	private void initHazelcast(HazelcastConfig config, HazelcastInitializer initializer) {
		if (config.isMember()) {
			try {
				Config hazelcastConfig = null;
				if (config.getConfigFilePath() != null) {
					hazelcastConfig = new XmlConfigBuilder(FileSystemUtils.createAbsolutePathFrom(
							System.getProperty("application.extensionsFolder", "extensions"), config.getExtensionName(),
							config.getConfigFilePath())).build();
				}
				if (initializer == null && config.getInitializerClass() != null) {
					if (hazelcastConfig == null) {
						hazelcastConfig = new Config();
					}
					initializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getInitializerClass());
				}
				if (initializer != null) {
					initializer.prepare(hazelcastConfig);
				}
				hazelcastInstances.put(config.getName(), hazelcastConfig == null ? Hazelcast.newHazelcastInstance()
						: Hazelcast.newHazelcastInstance(hazelcastConfig));
			} catch (Exception e) {
				getLogger().error("init hazelcast config error: ", e);
			}
		} else {
			try {
				ClientConfig hazelcastConfig = new XmlClientConfigBuilder(FileSystemUtils.createAbsolutePathFrom(
						System.getProperty("application.extensionsFolder", "extensions"), config.getExtensionName(),
						config.getConfigFilePath())).build();

				hazelcastInstances.put(config.getName(), HazelcastClient.newHazelcastClient(hazelcastConfig));
			} catch (Exception e) {
				getLogger().error("init hazelcast config error: ", e);
			}
		}
	}

	public void start(List<HazelcastConfig> hazelcastConfigs, List<RedisConfig> redisConfigs) {

		if (redisConfigs != null && redisConfigs.size() > 0) {
			redisConfigs.forEach(config -> {
				// jedis service
				this.jedisServices.put(config.getName(), createJedisService(config));
			});
		}

		if (hazelcastConfigs != null && hazelcastConfigs.size() > 0) {
			hazelcastConfigs.parallelStream().forEach(config -> {
				if (config.isLazyInit()) {
					this.lazyInitConfigs.put(config.getName(), config);
				} else {
					this.initHazelcast(config, null);
				}
			});
		}
	}

	public HazelcastInstance getHazelcastInstance(String name) {
		if (this.hazelcastInstances.containsKey(name)) {
			return this.hazelcastInstances.get(name);
		} else if (this.lazyInitConfigs.containsKey(name)) {
			this.initHazelcast(this.lazyInitConfigs.get(name), null);
			return this.hazelcastInstances.get(name);
		}
		return null;
	}

	public HazelcastInstance getHazelcastInstance(String name, HazelcastInitializer initializer) {
		if (this.hazelcastInstances.containsKey(name)) {
			return this.hazelcastInstances.get(name);
		} else if (this.lazyInitConfigs.containsKey(name)) {
			this.initHazelcast(this.lazyInitConfigs.get(name), initializer);
			return this.hazelcastInstances.get(name);
		}
		return null;
	}

	public JedisService getJedisServiceByName(String name) {
		return this.jedisServices.get(name);
	}

}
