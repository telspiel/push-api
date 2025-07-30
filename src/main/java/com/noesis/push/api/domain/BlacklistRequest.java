package com.noesis.push.api.domain;

public class BlacklistRequest {
	String userName;
	String mNumber;
	String apiKey;
	
	public BlacklistRequest() {
		
	}
	
	public BlacklistRequest(String userName, String mNumber, String apiKey) {
		super();
		this.userName = userName;
		this.mNumber = mNumber;
		this.apiKey = apiKey;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getmNumber() {
		return mNumber;
	}
	public void setmNumber(String mNumber) {
		this.mNumber = mNumber;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}