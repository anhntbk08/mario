package com.mario.config;

public class HazelcastConfig extends MarioBaseConfig {

	private boolean isMember;
	private String configFilePath;
	private String extensionName;
	private String initializerClass;
	private boolean lazyInit = false;

	public String getConfigFilePath() {
		return configFilePath;
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public boolean isMember() {
		return isMember;
	}

	public void setMember(boolean isMember) {
		this.isMember = isMember;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}

	public String getInitializerClass() {
		return initializerClass;
	}

	public void setInitializerClass(String initializerClass) {
		this.initializerClass = initializerClass;
	}

	public boolean isLazyInit() {
		return lazyInit;
	}

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}
}
