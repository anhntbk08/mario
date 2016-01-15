package com.mario.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.redisson.ClusterServersConfig;
import org.redisson.MasterSlaveServersConfig;
import org.redisson.Redisson;
import org.redisson.SentinelServersConfig;
import org.redisson.connection.RandomLoadBalancer;
import org.redisson.connection.RoundRobinLoadBalancer;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.mario.config.HazelcastConfig;
import com.mario.config.RedisConfig;
import com.nhb.common.BaseLoggable;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.utils.FileSystemUtils;
import com.nhb.common.vo.HostAndPort;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

public class CacheManager extends BaseLoggable {

	private static final String DEFAULT_REDIS_ENDPOINT = System.getProperty("redis.default.host", "127.0.0.1") + ":"
			+ System.getProperty("redis.default.port", "6379");

	private Map<String, HazelcastInstance> hazelcastInstances;
	private Map<String, JedisService> jedisServices;
	private Map<String, Redisson> redissons;

	public CacheManager() {
		this.hazelcastInstances = new HashMap<>();
		this.redissons = new HashMap<String, Redisson>();
		this.jedisServices = new HashMap<>();
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

		for (Redisson redisson : this.redissons.values()) {
			try {
				redisson.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Redisson createRedission(RedisConfig redisConfig) {
		org.redisson.Config config = new org.redisson.Config();
		switch (redisConfig.getRedisType()) {
		case CLUSTER:
			ClusterServersConfig clusterServerConfig = config.useClusterServers();
			clusterServerConfig.setScanInterval(redisConfig.getScanInterval());
			for (HostAndPort endpoint : redisConfig.getEndpoints()) {
				clusterServerConfig.addNodeAddress(endpoint.toString());
			}
			break;
		case MASTER_SLAVE:
			MasterSlaveServersConfig masterSlaveConfig = config.useMasterSlaveConnection();
			for (HostAndPort endpoint : redisConfig.getEndpoints()) {
				if (endpoint.isMaster()) {
					masterSlaveConfig.setMasterAddress(endpoint.toString());
				} else {
					masterSlaveConfig.addSlaveAddress(endpoint.toString());
				}
			}
			String loadBalancerType = redisConfig.getLoadBalancer() == null ? "random" : redisConfig.getLoadBalancer();
			if (loadBalancerType.equalsIgnoreCase("random")) {
				masterSlaveConfig.setLoadBalancer(new RandomLoadBalancer());
			} else if (loadBalancerType.equalsIgnoreCase("roundrobin")) {
				masterSlaveConfig.setLoadBalancer(new RoundRobinLoadBalancer());
			} else {
				throw new RuntimeException(
						"master slave redis servers load balancer type `" + loadBalancerType + "` isn't supported");
			}
			break;
		case SENTINEL:
			SentinelServersConfig sentielConnectionConfig = config.useSentinelConnection();
			sentielConnectionConfig.setMasterName(redisConfig.getMasterName());
			for (HostAndPort endpoint : redisConfig.getEndpoints()) {
				sentielConnectionConfig.addSentinelAddress(endpoint.toString());
			}
			break;
		case SINGLE:
			if (redisConfig.getEndpoints().size() > 0) {
				config.useSingleServer().setAddress(redisConfig.getEndpoints().get(0).toString());
			} else {
				getLogger().warn("enpoint config cannot be found, using default location " + DEFAULT_REDIS_ENDPOINT);
				config.useSingleServer().setAddress(DEFAULT_REDIS_ENDPOINT);
			}
			break;
		default:
			break;
		}
		return Redisson.create(config);
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

	public void start(List<HazelcastConfig> hazelcastConfigs, List<RedisConfig> redisConfigs) {

		if (redisConfigs != null && redisConfigs.size() > 0) {
			redisConfigs.forEach(config -> {
				// jedis service
				this.jedisServices.put(config.getName(), createJedisService(config));
				// redisson
				this.redissons.put(config.getName(), createRedission(config));
			});
		}

		if (hazelcastConfigs != null && hazelcastConfigs.size() > 0) {
			hazelcastConfigs.parallelStream().forEach(config -> {
				if (config.isMember()) {
					try {
						Config hazelcastConfig = new XmlConfigBuilder(FileSystemUtils.createAbsolutePathFrom(
								System.getProperty("application.extensionsFolder", "extensions"),
								config.getExtensionName(), config.getConfigFilePath())).build();
						hazelcastInstances.put(config.getName(), Hazelcast.newHazelcastInstance(hazelcastConfig));
					} catch (Exception e) {
						getLogger().error("init hazelcast config error: ", e);
					}
				} else {
					try {
						ClientConfig hazelcastConfig = new XmlClientConfigBuilder(
								FileSystemUtils.createAbsolutePathFrom(
										System.getProperty("application.extensionsFolder", "extensions"),
										config.getExtensionName(), config.getConfigFilePath())).build();

						hazelcastInstances.put(config.getName(), HazelcastClient.newHazelcastClient(hazelcastConfig));
					} catch (Exception e) {
						getLogger().error("init hazelcast config error: ", e);
					}
				}
			});
		}
	}

	public HazelcastInstance getHazelcastInstance(String name) {
		if (this.hazelcastInstances.containsKey(name)) {
			return this.hazelcastInstances.get(name);
		}
		return null;
	}

	public Redisson getRedisson(String name) {
		return this.redissons.get(name);
	}

	public JedisService getJedisServiceByName(String name) {
		return this.jedisServices.get(name);
	}

}
