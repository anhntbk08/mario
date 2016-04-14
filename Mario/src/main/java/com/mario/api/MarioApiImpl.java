package com.mario.api;

import com.hazelcast.core.HazelcastInstance;
import com.mario.cache.CacheManager;
import com.mario.cache.hazelcast.HazelcastInitializer;
import com.mario.entity.EntityManager;
import com.mario.entity.ManagedObject;
import com.mario.entity.MessageHandler;
import com.mario.entity.message.Message;
import com.mario.exceptions.ManagedObjectNotFoundException;
import com.mario.exceptions.MessageHandlerNotFoundException;
import com.mario.gateway.Gateway;
import com.mario.gateway.GatewayManager;
import com.mario.gateway.socket.SocketSession;
import com.mario.gateway.socket.SocketSessionManager;
import com.mario.monitor.MonitorAgent;
import com.mario.monitor.MonitorAgentManager;
import com.mario.producer.MessageProducerManager;
import com.mario.schedule.Scheduler;
import com.mongodb.MongoClient;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.db.cassandra.CassandraDataSource;
import com.nhb.common.db.cassandra.CassandraDatasourceManager;
import com.nhb.common.db.mongodb.MongoDBSourceManager;
import com.nhb.common.db.sql.DBIAdapter;
import com.nhb.common.db.sql.SQLDataSourceManager;
import com.nhb.messaging.MessageProducer;

class MarioApiImpl implements MarioApi {

	private CassandraDatasourceManager cassandraDatasourceManager;
	private SQLDataSourceManager sqlDatasourceManager;
	private MongoDBSourceManager mongoDBSourceManager;
	private Scheduler scheduler;
	private CacheManager cacheManager;
	private EntityManager entityManager;
	private SocketSessionManager sessionManager;
	private MonitorAgentManager monitorAgentManager;
	private MessageProducerManager producerManager;
	private GatewayManager gatewayManager;

	MarioApiImpl(SQLDataSourceManager dataSourceManager, CassandraDatasourceManager cassandraDatasourceManager,
			Scheduler scheduler, CacheManager cacheManager, MongoDBSourceManager mongoDBSourceManager,
			EntityManager entityManager, SocketSessionManager sessionManager, MonitorAgentManager monitorAgentManager,
			MessageProducerManager producerManager, GatewayManager gatewayManager) {
		this.sqlDatasourceManager = dataSourceManager;
		this.cassandraDatasourceManager = cassandraDatasourceManager;
		this.scheduler = scheduler;
		this.cacheManager = cacheManager;
		this.entityManager = entityManager;
		this.mongoDBSourceManager = mongoDBSourceManager;
		this.sessionManager = sessionManager;
		this.monitorAgentManager = monitorAgentManager;
		this.producerManager = producerManager;
		this.gatewayManager = gatewayManager;
	}

	@Override
	public DBIAdapter getDatabaseAdapter(String dataSourceName) {
		DBIAdapter result = new DBIAdapter(sqlDatasourceManager, dataSourceName);
		return result;
	}

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	@Override
	public HazelcastInstance getHazelcastInstance(String name) {
		return this.cacheManager.getHazelcastInstance(name);
	}

	@Override
	public PuElement call(String handlerName, PuElement request) {
		MessageHandler handler = this.entityManager.getMessageHandler(handlerName);
		if (handler != null) {
			return handler.interop(request);
		} else {
			throw new MessageHandlerNotFoundException("Message handler not found for name: " + handlerName);
		}
	}

	@Override
	public void request(String handlerName, Message message) {
		MessageHandler handler = this.entityManager.getMessageHandler(handlerName);
		if (handler != null) {
			handler.handle(message);
		} else {
			throw new MessageHandlerNotFoundException("Message handler not found for name: " + handlerName);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T acquireObject(String managedObjectName, PuObject requestParams) {
		if (managedObjectName != null) {
			ManagedObject managedObject = this.entityManager.getManagedObject(managedObjectName);
			if (managedObject != null) {
				Object result = managedObject.acquire(requestParams);
				if (result != null) {
					return (T) result;
				}
			} else {
				throw new ManagedObjectNotFoundException("ManagedObject not found for name: " + managedObjectName);
			}
		}
		return null;
	}

	@Override
	public void releaseObject(String managedObjectName, Object objectTobeReleased) {
		if (managedObjectName != null) {
			ManagedObject managedObject = this.entityManager.getManagedObject(managedObjectName);
			if (managedObject != null) {
				managedObject.release(objectTobeReleased);
			} else {
				throw new ManagedObjectNotFoundException("ManagedObject not found for name: " + managedObjectName);
			}
		}
	}

	@Override
	public MongoClient getMongoClient(String name) {
		assert name != null;
		assert this.mongoDBSourceManager != null;
		return this.mongoDBSourceManager.getMongoClient(name);
	}

	@Override
	public SocketSession getSocketSession(String sessionId) {
		return this.sessionManager.getSessionFromId(sessionId);
	}

	@Override
	public JedisService getJedisService(String name) {
		return this.cacheManager.getJedisServiceByName(name);
	}

	@Override
	public MonitorAgent getMonitorAgent(String name) {
		return this.monitorAgentManager == null ? null : this.monitorAgentManager.getMonitorAgent(name);
	}

	@Override
	public <T extends MessageProducer<?>> T getProducer(String name) {
		return this.producerManager.getProducer(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Gateway> T getGateway(String name) {
		return (T) this.gatewayManager.getGatewayByName(name);
	}

	@Override
	public CassandraDataSource getCassandraDataSource(String name) {
		return this.cassandraDatasourceManager == null ? null : this.cassandraDatasourceManager.getDatasource(name);
	}

	@Override
	public HazelcastInstance getHazelcastInstance(String name, HazelcastInitializer initializer) {
		return this.cacheManager.getHazelcastInstance(name, initializer);
	}

}
