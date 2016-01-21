package com.mario.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.mario.config.gateway.GatewayConfig;
import com.mario.config.gateway.GatewayType;
import com.mario.config.gateway.RabbitMQGatewayConfig;
import com.mario.config.gateway.SocketGatewayConfig;
import com.mario.entity.message.transcoder.MessageDecoder;
import com.mario.entity.message.transcoder.MessageEncoder;
import com.mario.extension.ExtensionManager;
import com.mario.gateway.http.HttpGateway;
import com.mario.gateway.rabbitmq.RabbitMQGatewayFactory;
import com.mario.gateway.serverwrapper.HasServerWrapper;
import com.mario.gateway.serverwrapper.ServerWrapperManager;
import com.mario.gateway.socket.SocketGateway;
import com.mario.gateway.socket.SocketGatewayFactory;
import com.mario.gateway.socket.SocketSessionManager;
import com.nhb.common.BaseLoggable;

public final class GatewayManager extends BaseLoggable {

	private static class GatewayFactory extends BaseLoggable {
		public Gateway newGateway(GatewayConfig config) {
			if (config != null) {
				GatewayType type = config.getType();
				Gateway result = null;
				switch (type) {
				case HTTP:
					result = new HttpGateway();
					break;
				case RABBITMQ:
					result = RabbitMQGatewayFactory.newRabbitGateway(((RabbitMQGatewayConfig) config));
					break;
				case SOCKET:
					result = SocketGatewayFactory.newSocketGateway(((SocketGatewayConfig) config));
					break;
				case KAFKA:
					
					break;
				default:
					break;
				}
				return result;
			}
			return null;
		}
	}

	private final GatewayFactory gatewayFactory = new GatewayFactory();
	private final Map<String, Gateway> gatewayByName = new HashMap<String, Gateway>();
	private ExecutorService gatewayThreadPool;
	private ExtensionManager extensionManager;
	private SocketSessionManager socketSessionManager;
	private ServerWrapperManager serverWrapperManager;

	public GatewayManager(ExtensionManager extensionManager, ServerWrapperManager serverWrapperManager) {
		this.extensionManager = extensionManager;
		this.serverWrapperManager = serverWrapperManager;
		this.socketSessionManager = new SocketSessionManager();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void init(List<GatewayConfig> gatewayConfigs)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		if (gatewayConfigs == null || gatewayConfigs.size() == 0) {
			getLogger().warn("no gateway config found");
			return;
		}

		for (GatewayConfig config : gatewayConfigs) {
			if (config.getName() == null || config.getName().trim().length() == 0) {
				getLogger().error("cannot init gateway with empty name", new Exception());
				continue;
			}
			Gateway gateway = this.gatewayFactory.newGateway(config);
			if (gateway instanceof AbstractGateway) {
				((AbstractGateway<?>) gateway).setExtensionName(config.getExtensionName());
				if (config.getDeserializerClassName() != null) {
					MessageDecoder deserializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getDeserializerClassName().trim());
					((AbstractGateway<?>) gateway).setDeserializer(deserializer);
				} else {
					throw new RuntimeException("cannot init gateway without MessageDeserializer");
				}
				if (config.getSerializerClassName() != null) {
					MessageEncoder deserializer = this.extensionManager.newInstance(config.getExtensionName(),
							config.getSerializerClassName().trim());
					((AbstractGateway<?>) gateway).setSerializer(deserializer);
				}
				if (gateway instanceof HasServerWrapper) {
					((HasServerWrapper) gateway)
							.setServer(this.serverWrapperManager.getServerWrapper(config.getServerWrapperName()));
				}
				if (gateway instanceof SocketGateway) {
					((SocketGateway) gateway).setSessionManager(this.socketSessionManager);
				}
			}
			gateway.init(config);
			this.gatewayByName.put(config.getName(), gateway);
		}
	}

	public void start() {
		if (gatewayByName.size() > 0) {
			this.gatewayThreadPool = new ThreadPoolExecutor(this.gatewayByName.size(), this.gatewayByName.size(), 60,
					TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

						final AtomicInteger threadNumber = new AtomicInteger(1);

						@Override
						public Thread newThread(Runnable r) {
							return new Thread(r, String.format("Gateway Starter #%d", threadNumber.getAndIncrement()));
						}
					});
			// parallel starting all the gateways
			this.gatewayByName.values().forEach(gateway -> {

				this.gatewayThreadPool.execute(new Runnable() {

					@Override
					public void run() {
						try {
							gateway.start();
						} catch (Exception e) {
							getLogger().error("cannot start gateway {}", gateway.getName(), e);
						}
					}
				});
			});
		} else {
			getLogger().warn("no gateway to start");
		}
	}

	public void stop() {
		for (Gateway gateway : this.gatewayByName.values()) {
			try {
				System.out.println("Stopping gateway " + gateway.getName());
				gateway.stop();
			} catch (Exception e) {
				getLogger().error("Cannot stop gateway: " + gateway.getName(), e);
				System.err.println("Cannot stop gateway: " + gateway.getName());
				e.printStackTrace();
			}
		}
		List<String> gatewayNames = new ArrayList<>(this.gatewayByName.keySet());
		for (String name : gatewayNames) {
			this.gatewayByName.remove(name);
		}

		if (gatewayThreadPool != null) {
			this.gatewayThreadPool.shutdown();
			try {
				if (this.gatewayThreadPool.awaitTermination(3, TimeUnit.SECONDS)) {
					System.err.println("cannot shutdown gateway threadpool, force by calling shutdownNow() method");
					this.gatewayThreadPool.shutdownNow();
					if (this.gatewayThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
						System.err.println("cannot shutdown gateway threadpool...");
					}
				}
			} catch (InterruptedException ex) {
				getLogger().error("error while waiting for gateway thread pool to shutdown", ex);
				System.err.println("error while waiting for gateway thread pool to shutdown");
				ex.printStackTrace();
			}
		}
	}

	public Gateway getGatewayByName(String name) {
		if (name != null && name.trim().length() > 0) {
			return this.gatewayByName.get(name.trim());
		}
		return null;
	}

	public Collection<Gateway> getGateways() {
		return this.gatewayByName.values();
	}

	public SocketSessionManager getSocketSessionManager() {
		return socketSessionManager;
	}
}
