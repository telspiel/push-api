package com.noesis.push.api.domain;

public class JsonRequestData {
		String text;
		String messagetype;
		String mobiles;
		String custref;
		String templateid;
		String hashId;
		
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getMessagetype() {
			return messagetype;
		}
		public void setMessagetype(String messagetype) {
			this.messagetype = messagetype;
		}
		public String getMobiles() {
			return mobiles;
		}
		public void setMobiles(String mobiles) {
			this.mobiles = mobiles;
		}
		public String getCustref() {
			return custref;
		}
		public void setCustref(String custref) {
			this.custref = custref;
		}
		public String getTemplateid() {
			return templateid;
		}
		public void setTemplateid(String templateid) {
			this.templateid = templateid;
		}
		public String getHashId() {
			return hashId;
		}
		public void setHashId(String hashId) {
			this.hashId = hashId;
		}
		
		
}
