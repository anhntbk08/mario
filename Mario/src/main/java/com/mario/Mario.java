package com.mario;

import java.lang.management.ThreadInfo;
import java.util.Arrays;

import org.nhb.core.thread.DeadLockMonitor;

import com.mario.api.MarioApiFactory;
import com.mario.cache.CacheManager;
import com.mario.config.MessageHandlerConfig;
import com.mario.entity.EntityManager;
import com.mario.extension.ExtensionManager;
import com.mario.gateway.Gateway;
import com.mario.gateway.GatewayManager;
import com.mario.gateway.serverwrapper.ServerWrapperManager;
import com.mario.monitor.MonitorAgentManager;
import com.mario.producer.MessageProducerManager;
import com.mario.schedule.impl.SchedulerFactory;
import com.nhb.common.BaseLoggable;
import com.nhb.common.db.cassandra.CassandraDatasourceManager;
import com.nhb.common.db.mongodb.MongoDBSourceManager;
import com.nhb.common.db.sql.SQLDataSourceConfig;
import com.nhb.common.db.sql.SQLDataSourceManager;
import com.nhb.common.utils.Initializer;

public final class Mario extends BaseLoggable {

	static {
		Initializer.bootstrap(Mario.class);
	}

	public static void main(String[] args) {
		final Mario app = new Mario();
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				{
					this.setName("Shutdown Thread");
					this.setPriority(MAX_PRIORITY);
				}

				@Override
				public void run() {
					app.stop();
				}
			});

			app.start();
		} catch (Exception e) {
			System.err.println("error while starting application: ");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private Mario() {

	}

	private boolean running = false;
	private ExtensionManager extensionManager;
	private GatewayManager gatewayManager;
	private EntityManager entityManager;
	private CassandraDatasourceManager cassandraDatasourceManager;
	private SQLDataSourceManager sqlDataSourceManager;
	private MarioApiFactory apiFactory;
	private SchedulerFactory schedulerFactory;
	private CacheManager cacheManager;
	private ServerWrapperManager serverWrapperManager;
	private MongoDBSourceManager mongoDBSourceManager;
	private MonitorAgentManager monitorAgentManager;
	private MessageProducerManager producerManager;

	private void start() throws Exception {
		if (this.running) {
			throw new IllegalAccessException("Application is already running");
		}

		this.schedulerFactory = SchedulerFactory.getInstance();
		this.mongoDBSourceManager = new MongoDBSourceManager();
		this.cacheManager = new CacheManager();
		this.serverWrapperManager = new ServerWrapperManager();
		this.producerManager = new MessageProducerManager(this.serverWrapperManager);
		this.sqlDataSourceManager = new SQLDataSourceManager();
		this.extensionManager = new ExtensionManager();
		this.extensionManager.load();

		this.cassandraDatasourceManager = new CassandraDatasourceManager();
		this.gatewayManager = new GatewayManager(this.extensionManager, this.serverWrapperManager);
		this.mongoDBSourceManager.addConfigs(this.extensionManager.getMongoDBConfigs());

		this.apiFactory = new MarioApiFactory(this.sqlDataSourceManager, this.cassandraDatasourceManager,
				this.schedulerFactory, this.mongoDBSourceManager, this.cacheManager, this.monitorAgentManager,
				this.producerManager, this.gatewayManager);

		this.monitorAgentManager = new MonitorAgentManager(apiFactory);

		for (SQLDataSourceConfig dataSourceConfig : this.extensionManager.getDataSourceConfigs()) {
			this.sqlDataSourceManager.registerDataSource(dataSourceConfig.getName(), dataSourceConfig.getProperties());
		}

		this.cassandraDatasourceManager.init(this.extensionManager.getCassandraConfigs());

		cacheManager.start(extensionManager.getHazelcastConfigs(), extensionManager.getRedisConfigs());

		this.serverWrapperManager.init(extensionManager.getServerWrapperConfigs());
		this.serverWrapperManager.start();

		this.producerManager.init(extensionManager.getProducerConfigs());
		this.producerManager.start();

		this.gatewayManager.init(extensionManager.getGatewayConfigs());

		this.entityManager = new EntityManager(this.extensionManager, this.apiFactory);
		this.entityManager.init();

		this.monitorAgentManager.init(extensionManager.getMonitorAgentConfigs());

		this.apiFactory.setGatewayManager(this.gatewayManager);
		this.bindGatewayToHandlers();

		this.gatewayManager.start();

		this.monitorAgentManager.start();

		this.running = true;

		this.startDeadlockMonitoring();
	}

	private void startDeadlockMonitoring() {
		// start dead lock monitoring
		DeadLockMonitor deadLockMonitor = new DeadLockMonitor();
		deadLockMonitor.addListener(new DeadLockMonitor.Listener() {

			@Override
			public void deadlockDetected(ThreadInfo paramThreadInfo) {
				getLogger().error("******** Deadlock detected ********\n" + paramThreadInfo.toString());
			}

			@Override
			public void thresholdExceeded(ThreadInfo[] paramArrayOfThreadInfo) {
				getLogger().warn(
						"******** Max thread threshold exeeded ********\n" + Arrays.asList(paramArrayOfThreadInfo));
			}
		});
	}

	private void bindGatewayToHandlers() {
		this.entityManager.getMessageHandlers().forEach(handler -> {
			MessageHandlerConfig config = entityManager.getConfig(handler.getName());
			config.getBindingGateways().forEach(gatewayName -> {
				Gateway gateway = gatewayManager.getGatewayByName(gatewayName);
				if (gateway == null) {
					throw new NullPointerException("Gateway can not be found for name: " + gatewayName);
				} else {
					handler.bind(gateway);
				}
			});
		});
	}

	private void stop() {
		if (this.schedulerFactory != null) {
			System.out.print("Stopping scheduler factory... ");
			this.schedulerFactory.stop();
			System.out.println("DONE");
		}
		if (this.producerManager != null) {
			System.out.print("Stopping producer manager... ");
			this.producerManager.stop();
			System.out.println("DONE");
		}
		if (this.serverWrapperManager != null) {
			System.out.print("Stopping server wrappers... ");
			this.serverWrapperManager.stop();
			System.out.println("DONE");
		}
		if (this.running) {
			System.out.print("Stopping gateways... ");
			this.gatewayManager.stop();
			System.out.println("DONE");
		}
		if (this.entityManager != null) {
			System.out.print("Destroying entities... ");
			this.entityManager.destroy();
			System.out.println("DONE");
		}
		if (this.cacheManager != null) {
			System.out.print("Stopping cache manager... ");
			this.cacheManager.stop();
			System.out.println("DONE");
		}
	}
}
