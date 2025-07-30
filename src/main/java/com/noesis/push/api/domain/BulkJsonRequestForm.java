package com.noesis.push.api.domain;

import java.util.ArrayList;

public class BulkJsonRequestForm {
	
	String var;
	String username;
	String apikey;
	ArrayList<String> msgtype;
	ArrayList<String> msgtext;
	ArrayList<String> signature;
	String entity;
	ArrayList<String> templateid;
	String tag;
	String tag1;
	String custref;
	String hashId;

	ArrayList<String> dest;

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

	

	public ArrayList<String> getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(ArrayList<String> msgtype) {
		this.msgtype = msgtype;
	}

	public ArrayList<String> getMsgtext() {
		return msgtext;
	}

	public void setMsgtext(ArrayList<String> msgtext) {
		this.msgtext = msgtext;
	}

	

	public ArrayList<String> getSignature() {
		return signature;
	}

	public void setSignature(ArrayList<String> signature) {
		this.signature = signature;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	

	public ArrayList<String> getTemplateid() {
		return templateid;
	}

	public void setTemplateid(ArrayList<String> templateid) {
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

	public String getCustref() {
		return custref;
	}

	public void setCustref(String custref) {
		this.custref = custref;
	}

	public ArrayList<String> getDest() {
		return dest;
	}

	public void setDest(ArrayList<String> dest) {
		this.dest = dest;
	}
	
	public String getHashId() {
		return hashId;
	}

	public void setHashId(String hashId) {
		this.hashId = hashId;
	}

	public BulkJsonRequestForm() {
		super();
		// TODO Auto-generated constructor stub
	}

	

}
