package com.debugchaos.azure_functions.entity;

import java.util.Objects;

public class UserConfig {

	private String id;
	private String username;
	private String accessToken;
	private String tokenSecret;
	private String bearerToken;
	private String refreshToken;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
	public String getBearerToken() {
		return bearerToken;
	}
	public void setBearerToken(String bearerToken) {
		this.bearerToken = bearerToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@Override
	public String toString() {
		return "UserConfig [id=" + id + ", username=" + username + ", accessToken=" + accessToken + ", tokenSecret="
				+ tokenSecret + ", bearerToken=" + bearerToken + ", refreshToken=" + refreshToken + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserConfig other = (UserConfig) obj;
		return Objects.equals(id, other.id);
	}
	
	
	

}
