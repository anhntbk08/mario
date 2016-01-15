package com.nhb.eventdriven.impl;

import java.lang.reflect.Method;

import com.nhb.common.BaseLoggable;
import com.nhb.eventdriven.Event;
import com.nhb.eventdriven.EventHandler;

public class BaseEventHandler extends BaseLoggable implements EventHandler {

	private Object handler;
	private String methodName;
	private Method targetMethod;

	public BaseEventHandler() {
		// do nothing
	}

	public BaseEventHandler(Object handler, String methodName) {
		this.handler = handler;
		this.methodName = methodName;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		if (targetMethod == null) {
			targetMethod = this.handler.getClass().getMethod(methodName, Event.class);
		}
		targetMethod.invoke(handler, event);
	}
}
