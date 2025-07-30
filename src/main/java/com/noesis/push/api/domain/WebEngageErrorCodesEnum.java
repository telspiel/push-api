package com.noesis.push.api.domain;

public enum WebEngageErrorCodesEnum {

	INSUFFICIENT_BALANCE("sms_rejected","2000","Not enough credit to send message"),
	INVALID_DESTINATION_NUMBER("sms_rejected","2003","Invalid mobile number"),
	INVALID_SENDER_ID("sms_rejected","2004","Invalid Sender ID"),
	DND_NUMBER_FOUND("sms_rejected","2006","User under DND"),
	INVALID_VERSION("sms_rejected","2010","Version not supported"),					//Add on code on requirement of Client (Aman)
	USER_BLACK_LIST_NUMBER_FOUND("sms_rejected","3000","Recipient Blacklisted"),
	
	MESSAGE_RECEIVED_POST_CUT_OFF("sms_rejected","102","MESSAGE_RECEIVED_POST_CUT_OFF"),
	
	RESTRICTED_CONTENT_FOUND("sms_rejected","104","RESTRICTED_CONTENT_FOUND"),
	
	
	GLOBAL_BLACK_LIST_NUMBER_FOUND("sms_rejected","107","GLOBAL_BLACK_LIST_NUMBER_FOUND"),
	USER_BLACK_LIST_SENDER_ID_FOUND("sms_rejected","108","USER_BLACK_LIST_SENDER_ID_FOUND"),
	GLOBAL_BLACK_LIST_SENDER_ID_FOUND("sms_rejected","109","GLOBAL_BLACK_LIST_SENDER_ID_FOUND"),
	
	NO_ACTIVE_KANNEL_FOUND("sms_rejected","111","NO_ACTIVE_KANNEL_FOUND"),
	KANNEL_NOT_REACHABLE("sms_rejected","112","KANNEL_NOT_REACHABLE"),
	CONTENT_TEMPLATE_MISMATCH("sms_rejected","113", "CONTENT_TEMPLATE_MISMATCH"),
	FAILED_NUMBER_FOUND("sms_rejected","120","FAILED_NUMBER_FOUND"),
	// API ERROR CODES
	INACTIVE_USER("sms_rejected","113","INACTIVE_USER"),
	INVALID_API_KEY("sms_rejected","2005","Authorization Failure"),
	INVALID_MESSAGE_TYPE("sms_rejected","116","INVALID_MESSAGE_TYPE"),
	INVALID_MESSAGE_TEXT("sms_rejected","2002","Empty Message Body"),
	INVALID_USER("sms_rejected","2015","Incorrect User Details"),
	UNKNOWN_REASON("sms_rejected","9988","Unknown Reason");
	
	
	
	
	private WebEngageErrorCodesEnum( String status,String statusCode, String message) {
		this.status = status;
		this.statusCode = statusCode;
		this.message = message;
	}
	
	private final String status;
	
	private final String statusCode;
	
	private final String message;

	public String getStatus() {
		return status;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public String getMessage() {
		return message;
	}
	
	
}
