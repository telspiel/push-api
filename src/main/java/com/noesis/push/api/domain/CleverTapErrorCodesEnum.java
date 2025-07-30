package com.noesis.push.api.domain;

public enum CleverTapErrorCodesEnum {
	
	INSUFFICIENT_BALANCE("2000","Not enough credit to send message"),
	INVALID_DESTINATION_NUMBER("2003","Invalid mobile number"),
	INVALID_SENDER_ID("2004","Invalid Sender ID"),
	
	
	DND_NUMBER_FOUND("901","DND failure"),
	SPAM_FOUND("902","Failed spam detected"),
	USER_BLACK_LIST_NUMBER_FOUND("903","Failed rejected blacklist"),
	SYSTEM_ERROR("904", "Failed system error"),
	SUBSCRIBER_ERROR("905", "Failed subscriber error"),
	DLT_BLOCKED("906", "Blocked by DLT"),
	ENTITY_BLOCKED("907", "Entity blocked by DLT"),
	TEMPLATE_BLOCKED("908", "Template blocked by DLT"),
	CONSENT_ERROR("909", "Failed DLT consent error"),
	INVALID_SUBSCRIBER("910", "Invalid subscriber"),
	INBOX_FULL("912", "Message Inbox Full"),
	NDNC_REJECTED("913", "NDNC Rejected"),
	MESSAGE_UNDELIVERED("914", "Undelivered"),
	DROPPED("915", "Dropped"),
	EXPIRED("916", "Expired"),
	FORCE_EXPIRED("917", "Force Expired"),
	DUPLICATE_MESSAGE("918", "Duplicate Message Drop"),
	DLT_FAILURE("919", "DLT Failure"),
	
	INVALID_VERSION("2010","Version not supported"),					//Add on code on requirement of Client (Aman)
	
	
	MESSAGE_RECEIVED_POST_CUT_OFF("102","MESSAGE_RECEIVED_POST_CUT_OFF"),
	
	RESTRICTED_CONTENT_FOUND("104","RESTRICTED_CONTENT_FOUND"),
	
	
	GLOBAL_BLACK_LIST_NUMBER_FOUND("107","GLOBAL_BLACK_LIST_NUMBER_FOUND"),
	USER_BLACK_LIST_SENDER_ID_FOUND("108","USER_BLACK_LIST_SENDER_ID_FOUND"),
	GLOBAL_BLACK_LIST_SENDER_ID_FOUND("109","GLOBAL_BLACK_LIST_SENDER_ID_FOUND"),
	
	NO_ACTIVE_KANNEL_FOUND("111","NO_ACTIVE_KANNEL_FOUND"),
	KANNEL_NOT_REACHABLE("112","KANNEL_NOT_REACHABLE"),
	CONTENT_TEMPLATE_MISMATCH("113", "CONTENT_TEMPLATE_MISMATCH"),
	FAILED_NUMBER_FOUND("120","FAILED_NUMBER_FOUND"),
	// API ERROR CODES
	INACTIVE_USER("113","INACTIVE_USER"),
	INVALID_API_KEY("2005","Authorization Failure"),
	INVALID_MESSAGE_TYPE("116","INVALID_MESSAGE_TYPE"),
	INVALID_MESSAGE_TEXT("2002","Empty Message Body"),
	INVALID_USER("2015","Incorrect User Details"),
	UNKNOWN_REASON("9988","Unknown Reason");
	
	
	
	
	private CleverTapErrorCodesEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

	
	private final String code;
	
	private final String description;


	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}


}
