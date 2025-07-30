package com.noesis.push.api.domain;

import java.util.ArrayList;

public class JsonApiPushRequestForm {
	String var;
	String username;
	String apikey;
	String msgtype;
	// String msgtext;
	String signature;
	String entity;
	String templateid;
	String tag;
	String tag1;
	private ArrayList<PushApiMsgsData> msgs;
	String hashId;

	// ArrayList<String> dest;
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getTemplateid() {
		return templateid;
	}

	public void setTemplateid(String templateid) {
		this.templateid = templateid;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTag1() {
		return tag1;
	}

	public void setTag1(String tag1) {
		this.tag1 = tag1;
	}

	public ArrayList<PushApiMsgsData> getMsgs() {
		return msgs;
	}

	public void setMsgs(ArrayList<PushApiMsgsData> msgs) {
		this.msgs = msgs;
	}	
	public String getHashId() {
		return hashId;
	}

	public void setHashId(String hashId) {
		this.hashId = hashId;
	}

	public JsonApiPushRequestForm() {
	}

}
