	package com.noesis.push.api.domain;


public class WebEngageJsonRequest {
	private String version;
	private WebEngageSmsData smsData;
	private WebEngageMetaData metadata;
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public WebEngageSmsData getSmsData() {
		return smsData;
	}
	public void setSmsData(WebEngageSmsData smsData) {
		this.smsData = smsData;
	}
	public WebEngageMetaData getMetadata() {
		return metadata;
	}
	public void setMetadata(WebEngageMetaData metadata) {
		this.metadata = metadata;
	}
	
	
	
	
	
	
	
}

