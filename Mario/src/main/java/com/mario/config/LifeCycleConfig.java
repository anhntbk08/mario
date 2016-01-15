package com.mario.config;

import com.nhb.common.data.PuObject;

public class LifeCycleConfig extends MarioBaseConfig {

	private PuObject initParams;
	private String handleClass;

	public PuObject getInitParams() {
		return initParams;
	}

	public void setInitParams(PuObject initParams) {
		this.initParams = initParams;
	}

	public String getHandleClass() {
		return handleClass;
	}

	public void setHandleClass(String handleClass) {
		this.handleClass = handleClass;
	}
}
