package com.nhb.common.db.beans;

import java.io.Serializable;

import com.google.gson.Gson;
import com.nhb.common.BaseLoggable;

public class AbstractBean extends BaseLoggable implements Serializable {

	private static final long serialVersionUID = -3242520191301712269L;
	protected static final Gson gson = new Gson();

	@Override
	public String toString() {
		return gson.toJson(this);
	}
}
