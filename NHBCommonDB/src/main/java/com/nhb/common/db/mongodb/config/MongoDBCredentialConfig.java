package com.nhb.common.db.mongodb.config;

import com.nhb.common.vo.UserNameAndPassword;

public class MongoDBCredentialConfig extends UserNameAndPassword {

	private String authDB = "admin";

	public MongoDBCredentialConfig() {
		// do nothing
	}

	public MongoDBCredentialConfig(String userName, String password) {
		this();
		this.setUserName(userName);
		this.setPassword(password);
	}

	public MongoDBCredentialConfig(String userName, String password, String authDB) {
		this(userName, password);
		this.setAuthDB(authDB);
	}

	public String getAuthDB() {
		return authDB;
	}

	public void setAuthDB(String authDB) {
		this.authDB = authDB;
	}
}
