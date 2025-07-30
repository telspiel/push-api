package com.noesis.push.api.domain;

import java.util.ArrayList;

public class JsonRequest {
	String username;
	String password;
	String senderid;
	String userdomain;
	String converturl;
	String campaignname;
	String callbackurl;
	String entityid;
	ArrayList<JsonRequestData> smslist;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSenderid() {
		return senderid;
	}
	public void setSenderid(String senderid) {
		this.senderid = senderid;
	}
	public String getUserdomain() {
		return userdomain;
	}
	public void setUserdomain(String userdomain) {
		this.userdomain = userdomain;
	}
	public String getConverturl() {
		return converturl;
	}
	public void setConverturl(String converturl) {
		this.converturl = converturl;
	}
	public String getCampaignname() {
		return campaignname;
	}
	public void setCampaignname(String campaignname) {
		this.campaignname = campaignname;
	}
	public String getCallbackurl() {
		return callbackurl;
	}
	public void setCallbackurl(String callbackurl) {
		this.callbackurl = callbackurl;
	}
	public ArrayList<JsonRequestData> getSmslist() {
		return smslist;
	}
	public void setSmslist(ArrayList<JsonRequestData> smslist) {
		this.smslist = smslist;
	}
	public String getEntityid() {
		return entityid;
	}
	public void setEntityid(String entityid) {
		this.entityid = entityid;
	}
	
}

