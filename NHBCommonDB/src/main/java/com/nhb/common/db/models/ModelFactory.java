package com.nhb.common.db.models;

import com.hazelcast.core.HazelcastInstance;
import com.mongodb.MongoClient;
import com.nhb.common.cache.jedis.JedisService;
import com.nhb.common.db.cassandra.daos.CassandraDAOFactory;
import com.nhb.common.db.sql.DBIAdapter;

public class ModelFactory {

	private ClassLoader classLoader = this.getClass().getClassLoader();

	private DBIAdapter dbAdapter;
	private JedisService jedisService;
	private HazelcastInstance hazelcast;
	private MongoClient mongoClient;

	private CassandraDAOFactory cassandraDAOFactory;

	public ModelFactory() {
		// do nothing;
	}

	public ModelFactory(DBIAdapter dbAdapter) {
		this.setDbAdapter(dbAdapter);
	}

	public ModelFactory(DBIAdapter dbAdapter, JedisService jedisService) {
		this.setDbAdapter(dbAdapter);
	}

	public <T extends AbstractModel> T getModel(String modelClass) {
		try {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) this.classLoader.loadClass(modelClass);
			T model = (T) clazz.newInstance();
			model.setDbAdapter(this.getDbAdapter());
			model.setJedisService(this.getJedisService());
			model.setHazelcast(this.getHazelcast());
			model.setMongoClient(this.mongoClient);
			model.setCassandraDAOFactory(this.getCassandraDAOFactory());
			model.silentInit();
			return model;
		} catch (Exception ex) {
			throw new RuntimeException("create model instance error: ", ex);
		}
	}

	public <T extends AbstractModel> T newModel(Class<T> modelClass) {
		return this.getModel(modelClass.getName());
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		if (this.classLoader != null) {
			if (this.cassandraDAOFactory != null) {
				this.cassandraDAOFactory.setClassLoader(this.classLoader);
			}
		}
	}

	public HazelcastInstance getHazelcast() {
		return hazelcast;
	}

	public void setHazelcast(HazelcastInstance hazelcast) {
		this.hazelcast = hazelcast;
	}

	public DBIAdapter getDbAdapter() {
		return dbAdapter;
	}

	public void setDbAdapter(DBIAdapter dbAdapter) {
		this.dbAdapter = dbAdapter;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public void setMongoClient(MongoClient mongoClient) {
		this.mongoClient = mongoClient;
	}

	public JedisService getJedisService() {
		return jedisService;
	}

	public void setJedisService(JedisService jedisService) {
		this.jedisService = jedisService;
	}

	public CassandraDAOFactory getCassandraDAOFactory() {
		return cassandraDAOFactory;
	}

	public void setCassandraDAOFactory(CassandraDAOFactory cassandraDAOFactory) {
		this.cassandraDAOFactory = cassandraDAOFactory;
		if (this.cassandraDAOFactory != null && this.classLoader != null) {
			this.cassandraDAOFactory.setClassLoader(this.classLoader);
		}
	}

}
