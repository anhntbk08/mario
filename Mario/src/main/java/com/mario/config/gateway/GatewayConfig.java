package com.mario.config.gateway;

import com.mario.config.MarioBaseConfig;
import com.mario.config.WorkerPoolConfig;

public abstract class GatewayConfig extends MarioBaseConfig {

	private String serverWrapperName;
	private String deserializerClassName;
	private String serializerClassName;
	private GatewayType type;
	private boolean ssl = false;
	private WorkerPoolConfig workerPoolConfig;

	public String getSerializerClassName() {
		return serializerClassName;
	}

	public void setSerializerClassName(String serializerClassName) {
		this.serializerClassName = serializerClassName;
	}

	public String getDeserializerClassName() {
		return deserializerClassName;
	}

	public void setDeserializerClassName(String deserializerClassName) {
		if (deserializerClassName == null || deserializerClassName.trim().length() == 0)
			return;
		this.deserializerClassName = deserializerClassName;
	}

	public GatewayType getType() {
		return type;
	}

	protected void setType(GatewayType type) {
		this.type = type;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public WorkerPoolConfig getWorkerPoolConfig() {
		return workerPoolConfig;
	}

	public void setWorkerPoolConfig(WorkerPoolConfig workerPoolConfig) {
		this.workerPoolConfig = workerPoolConfig;
	}

	public String getServerWrapperName() {
		return serverWrapperName;
	}

	public void setServerWrapperName(String serverWrapperName) {
		this.serverWrapperName = serverWrapperName;
	}

}
