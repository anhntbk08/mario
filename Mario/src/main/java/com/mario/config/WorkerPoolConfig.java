package com.mario.config;

public class WorkerPoolConfig {

	private String threadNamePattern = "Worker #%d";
	private int ringBufferSize = 1024;
	private int poolSize = 1;

	public String getThreadNamePattern() {
		return threadNamePattern;
	}

	public void setThreadNamePattern(String threadNamePattern) {
		this.threadNamePattern = threadNamePattern;
	}

	public int getRingBufferSize() {
		return ringBufferSize;
	}

	public void setRingBufferSize(int ringBufferSize) {
		this.ringBufferSize = ringBufferSize;
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public String toString() {
		return String.format("Thread name pattern: %s, ringbuffer size: %d, pool size: %d", this.getThreadNamePattern(),
				this.getRingBufferSize(), this.getPoolSize());
	}
}
