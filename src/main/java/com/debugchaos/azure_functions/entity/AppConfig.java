package com.debugchaos.azure_functions.entity;

import java.util.Objects;

public class AppConfig {

	private String id;
	private String appName;
	private String consumerKey;
	private String consumerSecret;
	private String bearerToken;
	private String clientId;
	private String clientSecret;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public String getBearerToken() {
		return bearerToken;
	}

	public void setBearerToken(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@Override
	public String toString() {
		return "AppConfig [id=" + id + ", appName=" + appName + ", consumerKey=" + consumerKey + ", consumerSecret="
				+ consumerSecret + ", bearerToken=" + bearerToken + ", clientId=" + clientId + ", clientSecret="
				+ clientSecret + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(appName, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppConfig other = (AppConfig) obj;
		return Objects.equals(appName, other.appName) && Objects.equals(id, other.id);
	}

}
