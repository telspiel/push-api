package com.noesis.push.api.domain;

public class PushRequest {
	String userName;
	String mNumber;
	String apiKey;
	String schTime;
	String signature;
	String custRef;
	String expiry;
	String msgType;
	String messageText;
	String domain;
	String convertUrl;
	String entityId;
	String dltTemplateId;
	String messageServiceType;
	String messageServiceSubType;
	String version;								//Add on code on requirement of Client (Aman)
	String hashId;
	
	
	public PushRequest(String userName, String mNumber, String apiKey, String schTime, String signature, String custRef,
			String expiry, String msgType, String msgTxt, String domain, String convertUrl, String entityId, String dltTemplateId, 
			String messageServiceType, String messageServiceSubType, String hashId) {
		super();
		this.userName = userName;
		this.mNumber = mNumber;
		this.apiKey = apiKey;
		this.schTime = schTime;
		this.signature = signature;
		this.custRef = custRef;
		this.expiry = expiry;
		this.msgType = msgType;
		this.messageText = msgTxt;
		this.domain = domain;
		this.convertUrl = convertUrl;
		this.entityId = entityId;
		this.dltTemplateId = dltTemplateId;
		this.messageServiceType = messageServiceType;
		this.messageServiceSubType = messageServiceSubType;
		this.hashId = hashId;
	}
	
	 
	public PushRequest(String userName, String mNumber, String apiKey, String schTime, String signature, String custRef,
			String expiry, String msgType, String msgTxt, String domain, String convertUrl,
			String messageServiceType, String messageServiceSubType) {
		super();
		this.userName = userName;
		this.mNumber = mNumber;
		this.apiKey = apiKey;
		this.schTime = schTime;
		this.signature = signature;
		this.custRef = custRef;
		this.expiry = expiry;
		this.msgType = msgType;
		this.messageText = msgTxt;
		this.domain = domain;
		this.convertUrl = convertUrl;
		this.messageServiceType = messageServiceType;
		this.messageServiceSubType = messageServiceSubType;
	}
	
	public PushRequest(String userName, String mNumber, String apiKey, String schTime, String signature, String custRef,
			String expiry, String msgType, String msgTxt,
			String messageServiceType, String messageServiceSubType) {
		super();
		this.userName = userName;
		this.mNumber = mNumber;
		this.apiKey = apiKey;
		this.schTime = schTime;
		this.signature = signature;
		this.custRef = custRef;
		this.expiry = expiry;
		this.msgType = msgType;
		this.messageText = msgTxt;
		this.messageServiceType = messageServiceType;
		this.messageServiceSubType = messageServiceSubType;
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
	public String getSchTime() {
		return schTime;
	} 
	public void setSchTime(String schTime) {
		this.schTime = schTime;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getCustRef() {
		return custRef;
	}
	public void setCustRef(String custRef) {
		this.custRef = custRef;
	}
	public String getExpiry() {
		return expiry;
	}
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String msgTxt) {
		this.messageText = msgTxt;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getConvertUrl() {
		return convertUrl;
	}

	public void setConvertUrl(String convertUrl) {
		this.convertUrl = convertUrl;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getDltTemplateId() {
		return dltTemplateId;
	}

	public void setDltTemplateId(String dltTemplateId) {
		this.dltTemplateId = dltTemplateId;
	}


	public String getMessageServiceType() {
		return messageServiceType;
	}


	public void setMessageServiceType(String messageServiceType) {
		this.messageServiceType = messageServiceType;
	}


	public String getMessageServiceSubType() {
		return messageServiceSubType;
	}


	public void setMessageServiceSubType(String messageServiceSubType) {
		this.messageServiceSubType = messageServiceSubType;
	}

	//Add on code on requirement of Client (Aman)
	public PushRequest(String userName, String mNumber, String apiKey, String schTime, String signature, String custRef,
			String expiry, String msgType, String msgTxt, String domain, String convertUrl, String entityId, String dltTemplateId, 
			String messageServiceType, String messageServiceSubType, String version, String hashId) {
		super();
		this.userName = userName;
		this.mNumber = mNumber;
		this.apiKey = apiKey;
		this.schTime = schTime;
		this.signature = signature;
		this.custRef = custRef;
		this.expiry = expiry;
		this.msgType = msgType;
		this.messageText = msgTxt;
		this.domain = domain;
		this.convertUrl = convertUrl;
		this.entityId = entityId;
		this.dltTemplateId = dltTemplateId;
		this.messageServiceType = messageServiceType;
		this.messageServiceSubType = messageServiceSubType;
		this.version = version;
		this.hashId = hashId;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public String getHashId() {
		return hashId;
	}


	public void setHashId(String hashId) {
		this.hashId = hashId;
	}
	
	
	
	
	//END
	
	
	
	
	
	
}