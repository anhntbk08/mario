package com.nhb.common.db.sql;

import java.util.Map.Entry;
import java.util.Properties;

import com.nhb.common.BaseLoggable;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.PuObject;

public class SQLDataSourceConfig extends BaseLoggable {

	private String name;
	private PuObject initParams;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PuObject getInitParams() {
		return initParams;
	}

	public void setInitParams(PuObject initParams) {
		this.initParams = initParams;
	}

	public Properties getProperties() {
		if (this.initParams != null) {
			Properties props = new Properties();
			for (Entry<String, PuValue> entry : this.initParams) {
				props.setProperty(entry.getKey(), entry.getValue().getData().toString());
			}
			return props;
		}
		return null;
	}
}
