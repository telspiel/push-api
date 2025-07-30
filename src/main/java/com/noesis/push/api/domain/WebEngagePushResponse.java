package com.noesis.push.api.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;



@JsonInclude(Include.NON_NULL)
public class WebEngagePushResponse {
	
	String version;
	String messageId;
	String toNumber;
	String status;
	String statusCode;
	String message;
	String supportedVersion;
	
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getToNumber() {
		return toNumber;
	}
	public void setToNumber(String toNumber) {
		this.toNumber = toNumber;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSupportedVersion() {
		return supportedVersion;
	}
	public void setSupportedVersion(String supportedVersion) {
		this.supportedVersion = supportedVersion;
	}
	
	
	
	@Override
	public String toString() {
		return "WebEngagePushResponse [version=" + version + ", messageId=" + messageId + ", toNumber=" + toNumber
				+ ", status=" + status + ", statusCode=" + statusCode + ", message=" + message + ", supportedVersion="
				+ supportedVersion + "]";
	}
	
	
}
