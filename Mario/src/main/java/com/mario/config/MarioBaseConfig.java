package com.mario.config;

import com.nhb.common.BaseLoggable;

public class MarioBaseConfig extends BaseLoggable {

	private String extensionName;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtensionName() {
		return extensionName;
	}

	public void setExtensionName(String extensionName) {
		this.extensionName = extensionName;
	}
}
