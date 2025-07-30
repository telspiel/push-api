package com.noesis.push.api.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class JsonResponseFormData {
	 String code;
	 String desc;
	 String reqId;
	 String time;
	 @JsonInclude(Include.NON_EMPTY)
		List<String> partMessageIds;
		
		int totalMessageParts;
//	 extra parameter add
		@JsonInclude(Include.NON_EMPTY)
		String custRef;
		@JsonInclude(Include.NON_EMPTY)
		String longUrl;
		@JsonInclude(Include.NON_EMPTY)
		String shortUrl;
		
		String hashId;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getReqId() {
		return reqId;
	}
	public void setReqId(String reqId) {
		this.reqId = reqId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	public List<String> getPartMessageIds() {
		return partMessageIds;
	}
	public void setPartMessageIds(List<String> partMessageIds) {
		this.partMessageIds = partMessageIds;
	}
	public int getTotalMessageParts() {
		return totalMessageParts;
	}
	public void setTotalMessageParts(int totalMessageParts) {
		this.totalMessageParts = totalMessageParts;
	}
	public String getCustRef() {
		return custRef;
	}
	public void setCustRef(String custRef) {
		this.custRef = custRef;
	}
	public String getLongUrl() {
		return longUrl;
	}
	public void setLongUrl(String longUrl) {
		this.longUrl = longUrl;
	}
	public String getShortUrl() {
		return shortUrl;
	}
	public void setShortUrl(String shortUrl) {
		this.shortUrl = shortUrl;
	}
	public String getHashId() {
		return hashId;
	}
	public void setHashId(String hashId) {
		this.hashId = hashId;
	}
	
	
}
