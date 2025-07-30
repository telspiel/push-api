package com.noesis.push.api.domain;

public class WebEngageMetaData {
	
	private String campaignType;
	private String timestamp;
	private String messageId;
	private String type;
	private WebEngageIndiaDlt indiaDLT;
	private WebEngageCustomData custom;
	
	public String getCampaignType() {
		return campaignType;
	}
	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	public WebEngageIndiaDlt getIndiaDLT() {
		return indiaDLT;
	}
	public void setIndiaDLT(WebEngageIndiaDlt indiaDLT) {
		this.indiaDLT = indiaDLT;
	}
	public WebEngageCustomData getCustom() {
		return custom;
	}
	public void setCustom(WebEngageCustomData custom) {
		this.custom = custom;
	}
	
	
}
