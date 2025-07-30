package com.noesis.push.api.controller;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noesis.domain.persistence.NgShortUrlChildMapping;
import com.noesis.domain.persistence.NgUser;
import com.noesis.domain.persistence.NgUserDomainMapping;
import com.noesis.domain.persistence.NgUserSenderIdMap;
import com.noesis.domain.platform.MessageObject;
import com.noesis.domain.service.NgShortUrlChildMappingService;
import com.noesis.domain.service.ShortUrlService;
import com.noesis.domain.service.UserBlackListService;
import com.noesis.domain.service.UserDomainMappingService;
import com.noesis.domain.service.UserSenderIdMapService;
import com.noesis.domain.service.UserService;
import com.noesis.push.api.domain.PushRequest;
import com.noesis.push.api.domain.WebEngageErrorCodesEnum;
import com.noesis.push.api.domain.WebEngagePushResponse;
import com.noesis.push.api.domain.WebEngageJsonRequest;
import com.noesis.push.api.kafka.MessageProducer;

@RestController
public class WebEngageJsonPushApiController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${push.api.instance.id}")
	private String instanceId;

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MessageProducer producer;

	@Autowired
	private ShortUrlService shortUrlService;

	@Autowired
	private NgShortUrlChildMappingService ngShortUrlChildMappingService;

	@Autowired
	private UserDomainMappingService userDomainMappingService;

	@Autowired
	UserSenderIdMapService userSenderIdMapService;

	@Autowired
	UserBlackListService userBlackListService;

	@Value("${push.api.kafka.topic.trans}")
	private String transTopicName;

	@Value("${push.api.kafka.topic.promo}")
	private String promoTopicName;

	@Value("${push.api.kafka.topic.transpromo}")
	private String transPromoTopicName;

	@Value("${push.api.supported.version}")
	private String version;

	@RequestMapping(value = "/json/sendwebengagesmsmsg", method = { RequestMethod.OPTIONS, RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	public String sendbulkmsg(@RequestBody WebEngageJsonRequest jsonRequest, @RequestHeader Map<String, String> headers,
			HttpServletResponse resp) throws JsonProcessingException {
		WebEngagePushResponse response = new WebEngagePushResponse();
		ArrayList<WebEngagePushResponse> responseList = new ArrayList<>();
		try {
			if (headers.get("username") != null && headers.get("api-key") != null) {

				String userName = headers.get("username");
				String apiKey = headers.get("api-key");
				String senderId = jsonRequest.getSmsData().getFromNumber();

				String userDomain = null;
				String convertUrl = "Y";
				String campaignName = "";
				String callbackUrl = "";
				String scheduleTime = null;
				String expiryMinutes = null;
				String version = jsonRequest.getVersion(); // Add on code on requirement of Client (Aman)
				
				String mNumberList = jsonRequest.getSmsData().getToNumber();
				String custRef = jsonRequest.getMetadata().getMessageId();
				String messageType = "PM";
				if (jsonRequest.getMetadata().getCustom() != null
						&& jsonRequest.getMetadata().getCustom().getTxt() != null) {
					if (jsonRequest.getMetadata().getCustom().getTxt().equals("0")) {
						messageType = "UC";
					}
				}

				String messageText = jsonRequest.getSmsData().getBody();
				String entityId = jsonRequest.getMetadata().getIndiaDLT().getPrincipalEntityId();
				String templateId = jsonRequest.getMetadata().getIndiaDLT().getContentTemplateId();
				String hashId = jsonRequest.getMetadata().getIndiaDLT().getHashId();
				DateFormat df = new SimpleDateFormat("ddMMyyyy");
				if (convertUrl == null) {
					convertUrl = "Y";
				}

				logger.info("Json Push Request received for user: {}, destination {}", userName, mNumberList);

				String mNumber = mNumberList;
				if (mNumber.startsWith("+")) {
					mNumber = mNumber.substring(1);
				}
				if (mNumber.contains(" ")) {
					mNumber = mNumber.replaceAll("\\s", "");
				}
				NgUser ngUser = userService.getUserByName(userName);

				NgUserSenderIdMap ngUserSenderIdMap = userSenderIdMapService.getSenderIdByIdAndUserId(senderId,
						ngUser.getId());
				String ngServiceType = null;
				if (ngUserSenderIdMap != null && ngUserSenderIdMap.getNgServiceType() != null) {
					ngServiceType = ngUserSenderIdMap.getNgServiceType().getDisplayCode();
				}
				PushRequest request = new PushRequest(userName, mNumber, apiKey, scheduleTime, senderId, custRef,
						expiryMinutes, messageType, messageText, userDomain, convertUrl, entityId, templateId,
						ngServiceType, null, version, hashId); // Add on code on requirement of Client (Aman)

				String messageSource = "pushapi-0-" + ngUser.getUserName() + "-campaign-" + df.format(new Date());
				if (campaignName == null) {
					campaignName = ngUser.getUserName() + "-campaign-" + df.format(new Date());
				}

				boolean validateRequest = validateRequest(request, response, ngUser, callbackUrl, campaignName);
				if (validateRequest) {
					processRequest(request, response, ngUser, messageSource);
				}

			} else {
				response.setStatus(WebEngageErrorCodesEnum.UNKNOWN_REASON.getStatus());
				response.setStatusCode(WebEngageErrorCodesEnum.UNKNOWN_REASON.getStatusCode());
				response.setMessage(WebEngageErrorCodesEnum.UNKNOWN_REASON.getMessage());

			}
		} catch (Exception e) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_USER.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_USER.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_USER.getMessage());
		}

		responseList.add(response);

		return objectMapper.writeValueAsString(response);
	}

	/**
	 * @author Aman
	 * @param jsonRequest
	 * @param headers
	 * @param resp
	 * @param authorizationHeader
	 * @return Expected response
	 * @throws JsonProcessingException
	 */

	@RequestMapping(value = "/json/sendWebEngagesMsg", method = { RequestMethod.OPTIONS, RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	public String sendBulkMsg(@RequestBody WebEngageJsonRequest jsonRequest, @RequestHeader Map<String, String> headers,
			HttpServletResponse resp, @RequestHeader("Authorization") String authorizationHeader)
			throws JsonProcessingException {
		WebEngagePushResponse response = new WebEngagePushResponse();
		ArrayList<WebEngagePushResponse> responseList = new ArrayList<>();
		try {

			if (headers.get("username") != null) {
				String userName = headers.get("username");

				String senderId = jsonRequest.getSmsData().getFromNumber();
				String userDomain = null;
				String convertUrl = "Y";
				String campaignName = "";
				String callbackUrl = "";
				String scheduleTime = null;
				String expiryMinutes = null;
				String version = jsonRequest.getVersion(); // Add on code on requirement of Client (Aman)
				String apiKey = extractBearer(authorizationHeader); // Write a method for Client Requirement, Replace
																	// api-key with Authorization Header

				String mNumberList = jsonRequest.getSmsData().getToNumber();
				String custRef = jsonRequest.getMetadata().getMessageId();
				String messageType = "PM";
				if (jsonRequest.getMetadata().getCustom() != null
						&& jsonRequest.getMetadata().getCustom().getTxt() != null) {
					if (jsonRequest.getMetadata().getCustom().getTxt().equals("0")) {
						messageType = "UC";
					}
				}

				String messageText = jsonRequest.getSmsData().getBody();
				String entityId = jsonRequest.getMetadata().getIndiaDLT().getPrincipalEntityId();
				String templateId = jsonRequest.getMetadata().getIndiaDLT().getContentTemplateId();
				String hashId = jsonRequest.getMetadata().getIndiaDLT().getHashId();
				DateFormat df = new SimpleDateFormat("ddMMyyyy");
				if (convertUrl == null) {
					convertUrl = "Y";
				}

				logger.info("Json Push Request received for user: {}, destination {}", userName, mNumberList);

				String mNumber = mNumberList;
				if (mNumber.startsWith("+")) {
					mNumber = mNumber.substring(1);
				}
				if (mNumber.contains(" ")) {
					mNumber = mNumber.replaceAll("\\s", "");
				}
				NgUser ngUser = userService.getUserByName(userName);

				NgUserSenderIdMap ngUserSenderIdMap = userSenderIdMapService.getSenderIdByIdAndUserId(senderId,
						ngUser.getId());
				String ngServiceType = null;
				if (ngUserSenderIdMap != null && ngUserSenderIdMap.getNgServiceType() != null) {
					ngServiceType = ngUserSenderIdMap.getNgServiceType().getDisplayCode();
				}
				PushRequest request = new PushRequest(userName, mNumber, apiKey, scheduleTime, senderId, custRef,
						expiryMinutes, messageType, messageText, userDomain, convertUrl, entityId, templateId,
						ngServiceType, null, version, hashId); // Add on code on requirement of Client (Aman)

				String messageSource = "pushapi-0-" + ngUser.getUserName() + "-campaign-" + df.format(new Date());
				if (campaignName == null) {
					campaignName = ngUser.getUserName() + "-campaign-" + df.format(new Date());
				}

				boolean validateRequest = validateRequest(request, response, ngUser, callbackUrl, campaignName);
				if (validateRequest) {
					processRequest(request, response, ngUser, messageSource);
				}

			} else {
				response.setStatus(WebEngageErrorCodesEnum.UNKNOWN_REASON.getStatus());
				response.setStatusCode(WebEngageErrorCodesEnum.UNKNOWN_REASON.getStatusCode());
				response.setMessage(WebEngageErrorCodesEnum.UNKNOWN_REASON.getMessage());

			}
		} catch (Exception e) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_USER.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_USER.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_USER.getMessage());
		}

		responseList.add(response);

		return objectMapper.writeValueAsString(response);
	}

	private boolean validateRequest(PushRequest request, WebEngagePushResponse response, NgUser ngUser,
			String callbackUrl, String campaignName) {

		if (ngUser == null || request.getUserName() == null || !request.getUserName().equals(ngUser.getUserName())) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_USER.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_USER.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_USER.getMessage());
			return false;
		}

		if (request.getApiKey() == null || !request.getApiKey().equals(ngUser.getEncryptedPassword())) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_API_KEY.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_API_KEY.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_API_KEY.getMessage());
			return false;
		}

		// Add on code on requirement of Client (Aman)
		if (!version.equals(request.getVersion()) || request.getVersion() == null) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_VERSION.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_VERSION.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_VERSION.getMessage());
			response.setSupportedVersion(version);
			return false;
		}
		// END

		if (request.getmNumber() == null
				|| (request.getmNumber().length() == 12 && !request.getmNumber().startsWith("91"))
				|| !Pattern.compile("\\d+").matcher(request.getmNumber()).matches()
				|| (request.getmNumber().length() == 11 && !request.getmNumber().startsWith("0"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("1"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("2"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("3"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("4"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("5"))) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_DESTINATION_NUMBER.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_DESTINATION_NUMBER.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_DESTINATION_NUMBER.getMessage());
			return false;
		}

		if (request.getmNumber() == null || request.getmNumber().length() < 10
				|| request.getmNumber().trim().length() > 12
				|| !Pattern.compile("\\d+").matcher(request.getmNumber()).matches()) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_DESTINATION_NUMBER.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_DESTINATION_NUMBER.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_DESTINATION_NUMBER.getMessage());
			return false;
		}

		if (request.getMessageText() == null || request.getMessageText().length() == 0) {
//				response.setMessage("Version not supported");
//				response.setSupportedVersion(version);
			response.setStatus(WebEngageErrorCodesEnum.INVALID_MESSAGE_TEXT.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_MESSAGE_TEXT.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_MESSAGE_TEXT.getMessage());
			return false;
		}

		if (request.getMsgType() == null) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_MESSAGE_TYPE.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_MESSAGE_TYPE.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_MESSAGE_TYPE.getMessage());
			return false;
		}

		if (request.getSignature() == null || request.getSignature().length() < 3
				|| request.getSignature().length() > 14) { // Changes Sender Id Length(Aman)
			response.setStatus(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getMessage());
			return false;
		} else if (request.getMessageServiceType() != null && !(request.getMessageServiceType().equals("promo"))) {
			Matcher m2 = Pattern.compile("^(?![0-9]*$)[a-zA-Z0-9]+$").matcher(request.getSignature());
			if (!(m2.matches())) {
				response.setStatus(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatus());
				response.setStatusCode(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatusCode());
				response.setMessage(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getMessage());
				return false;
			}
		} else if (request.getMessageServiceType() != null && request.getMessageServiceType().equals("promo")) {
			Matcher m1 = Pattern.compile("\\d{3,14}").matcher(request.getSignature());
			if (!(m1.matches())) {
				response.setStatus(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatus());
				response.setStatusCode(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatusCode());
				response.setMessage(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getMessage());
				return false;
			}
		} else {
			logger.info("Message service type received others.");
			Matcher m2 = Pattern.compile("^(?![0-9]*$)[a-zA-Z0-9]+$").matcher(request.getSignature());

			if (m2.matches()) {
				request.setMessageServiceType("trans");
				return true;
			}

			m2 = Pattern.compile("\\d{3,14}").matcher(request.getSignature());
			if (m2.matches()) {
				request.setMessageServiceType("promo");
				return true;
			}
			response.setStatus(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_SENDER_ID.getMessage());
			return false;
		}

		if ((request.getMessageText().contains("http://") || request.getMessageText().contains("https://"))
				&& ngUser.getIsVisualizeAllowed() == 'Y' && request.getConvertUrl().equalsIgnoreCase("Y")) {
			List<String> longUrls = SendOneToOneMessageController.extractUrls(request.getMessageText());

			logger.info("Found {} long URLs in the message.", longUrls.size());

			if (longUrls != null && longUrls.size() > 0) {
				for (String longUrl : longUrls) {
					logger.info("Processing long URL :" + longUrl);
					String finalHostName = "gmly.in";

					// Convert longUrl To shortUrl.
					if (request.getDomain() != null) {
						NgUserDomainMapping userDomainMapping = userDomainMappingService
								.isDomainNameActiveAndApproved(ngUser.getId(), request.getDomain(), 'Y', 'Y');
						if (userDomainMapping != null) {
							finalHostName = userDomainMapping.getDomainName();
							logger.info("Using custom domain : " + finalHostName);
						}
					} else {
						List<NgUserDomainMapping> ngUserDomainMappingList = userDomainMappingService
								.findAllDefaultHostByPlatform();
						if (ngUserDomainMappingList != null && ngUserDomainMappingList.size() > 0) {
							finalHostName = ngUserDomainMappingList.get(0).getDomainName();
							logger.info("Using default domain : " + finalHostName);
						}
					}
					String uniqueId = shortUrlService.shortenURL(finalHostName, longUrl, ngUser.getId(), 'Y');

					String senderId = request.getSignature();
					String shortenedUrl = finalHostName + "/" + senderId + "/" + uniqueId;
					logger.info("Shortened URL : " + shortenedUrl);
					String custRef = request.getCustRef();
					// Replace each long URL with its short URL in the message text
					request.setMessageText(request.getMessageText().replace(longUrl, shortenedUrl));
					NgShortUrlChildMapping ngShortUrlChildMapping = SendOneToOneMessageController
							.generateNgShortUrlChildMappingObject(ngUser, campaignName, null, uniqueId, shortenedUrl,
									request.getmNumber(), null, callbackUrl, finalHostName, custRef);
					ngShortUrlChildMappingService.saveChildShortUrlMapping(ngShortUrlChildMapping, longUrl, 'Y');
					logger.info("Saved short URL mapping for : " + longUrl);
				}
			} else {
				logger.warn("No long URLs found in the message.");
			}
//			if (longUrls != null && longUrls.size() > 0) {
//				// response.setLongUrl(longUrls.get(0));
//				String finalHostName = "gmly.in";
//				// convert longUrl To Short Url.
//				if (request.getDomain() != null) {
//					NgUserDomainMapping userDomainMapping = userDomainMappingService
//							.isDomainNameActiveAndApproved(ngUser.getId(), request.getDomain(), 'Y', 'Y');
//					if (userDomainMapping != null) {
//						finalHostName = userDomainMapping.getDomainName();
//					} else {
//						finalHostName = "gmly.in";
//					}
//				} else {
//					List<NgUserDomainMapping> ngUserDomainMappingList = userDomainMappingService
//							.findAllDefaultHostByPlatform();
//					if (ngUserDomainMappingList != null && ngUserDomainMappingList.size() > 0) {
//						finalHostName = ngUserDomainMappingList.get(0).getDomainName();
//					} else {
//						finalHostName = "gmly.in";
//					}
//				}
//				String uniqueId = shortUrlService.shortenURL(finalHostName, longUrls.get(0), ngUser.getId(), 'Y');
//				
//				String senderId = request.getSignature();
//				String shortenedUrl = finalHostName + "/" + senderId + "/" + uniqueId; // messageText =
//				logger.info("Shortened URL: {}", shortenedUrl);
//				
//				request.setMessageText(request.getMessageText().replaceAll(longUrls.get(0), shortenedUrl));
//				// response.setShortUrl(shortenedUrl);
//
//				NgShortUrlChildMapping ngShortUrlChildMapping = SendOneToOneMessageController
//						.generateNgShortUrlChildMappingObject(ngUser, campaignName, null, uniqueId, shortenedUrl,
//								request.getmNumber(), null, callbackUrl);
//
//				ngShortUrlChildMappingService.saveChildShortUrlMapping(ngShortUrlChildMapping, longUrls.get(0), 'Y');
//			}
		}
		return true;

	}

	private void processRequest(PushRequest request, WebEngagePushResponse response, NgUser ngUser1,
			String messageSource) {
		try {
			String messageId = request.getCustRef();
			// response.setMessageId(messageId);

			String message = generateJsonMessage(request, ngUser1, messageId, response, messageSource);

			// START (For Multiple Kafka Topic -: Aman)
			if (!(request.getMessageServiceType().equals("promo"))) {
//				producer.send(transTopicName, message);
//				logger.info("Message Sent To Trans Queue Successfully: " + message);
				NgUser ngUser = userService.getUserByNameFromDb(ngUser1.getUserName());
				if (ngUser.getKafkaPriority().equals("0")) {
					logger.info("Message Sent To Topic Successfully: " + ngUser.getKafkaPriority());
					producer.send(transTopicName, message);
					logger.info("Message Sent To Trans Queue Successfully: " + message);
				} else {
					String kafkapriority = ngUser.getKafkaPriority();
					String kafkaTopic = transTopicName + kafkapriority;
					logger.info("Message Sent To Topic Successfully: " + kafkaTopic);
					producer.send(kafkaTopic, message);
					logger.info("Message Sent To Trans Queue Successfully: " + message);
				}
			} else if ((request.getMessageServiceType().equals("promo"))) {
//				producer.send(promoTopicName, message);
//				logger.info("Message Sent To Promo Queue Successfully: " + message);
				NgUser ngUser = userService.getUserByNameFromDb(ngUser1.getUserName());
				if (ngUser.getKafkaPriority().equals("0")) {
					logger.info("Message Sent To Topic Successfully: " + ngUser.getKafkaPriority());
					producer.send(promoTopicName, message);
					logger.info("Message sent to Promo Queue Successfully: " + message);
				} else {
					String kafkaPriority = ngUser.getKafkaPriority();
					String kafkaTopic = promoTopicName + kafkaPriority;
					logger.info("Message Sent To Topic Successfully: " + kafkaTopic);
					producer.send(kafkaTopic, message);
					logger.info("Message sent to Promo Queue Successfully: " + message);
				}
			} else if (request.getMessageServiceType().equals("service") && ngUser1.getIsDndCheck() == 'Y') {
//				producer.send(transPromoTopicName, message);
//				logger.info("Message Sent To TransPromo Queue Successfully: " + message);
				NgUser ngUser = userService.getUserByNameFromDb(ngUser1.getUserName());
				if (ngUser.getKafkaPriority().equals("0")) {
					logger.info("Message Sent To Topic Successfully: " + ngUser.getKafkaPriority());
					producer.send(transPromoTopicName, message);
					logger.info("Message Sent To TransPromo Queue Successfully: " + message);
				} else {
					String kafkaPriority = ngUser.getKafkaPriority();
					String kafkaTopic = transPromoTopicName + kafkaPriority;
					logger.info("Message Sent To Topic Successfully: " + kafkaTopic);
					producer.send(kafkaTopic, message);
					logger.info("Message Sent To TransPromo Queue Successfully: " + message);
				}
				// END(For Multiple Kafka Topic -: Aman)
			}

			response.setStatus("sms_accepted");
		} catch (Exception e) {
			response.setStatus(WebEngageErrorCodesEnum.INVALID_USER.getStatus());
			response.setStatusCode(WebEngageErrorCodesEnum.INVALID_USER.getStatusCode());
			response.setMessage(WebEngageErrorCodesEnum.INVALID_USER.getMessage());
		}
	}

	private String generateJsonMessage(PushRequest request, NgUser ngUser, String messageId,
			WebEngagePushResponse response, String messageSource) throws JsonProcessingException {
		String mNumber = request.getmNumber();
		MessageObject msgObject = new MessageObject();
		msgObject.setSenderId(request.getSignature());
		msgObject.setMessage(request.getMessageText());
		msgObject.setMessageId(messageId);
		// msgObject.setAccountType(ngUser.getNgAccountType().getCode());
		msgObject.setMessageServiceType(request.getMessageServiceType());
		msgObject.setMessageServiceSubType(request.getMessageServiceSubType());
		msgObject.setHashId(request.getHashId());
		msgObject.setDestNumber(request.getmNumber());
		msgObject.setUsername(ngUser.getUserName());
		msgObject.setUserId(ngUser.getId());
		msgObject.setSplitCount(1);
		int messageLength = request.getMessageText().length();
		if (request.getMsgType().equalsIgnoreCase("pm")) {
			char[] messageCharArray = request.getMessageText().toCharArray();
			for (int i = 0; i < messageCharArray.length; i++) {
				char temp = messageCharArray[i];
				if (temp == '[' || temp == ']' || temp == '^' || temp == '{' || temp == '}'
						|| temp == '\\' || temp == '~' || temp == '|' || temp == '€') {
					messageLength++;
				}
			}
			logger.info("new message length with temp char : {}", messageLength);
			msgObject.setMessageType("PM");
			if (messageLength > 160) {
				int quotient = messageLength / 153;
				int messageSplitCount = quotient + 1;
				msgObject.setIsConcatMessageIndicator(true);
				msgObject.setSplitCount(messageSplitCount);
			}
		} else if (request.getMsgType().equalsIgnoreCase("fl")) {
			msgObject.setMessageType("FL");
			if (request.getMessageText().length() > 160) {
				int quotient = request.getMessageText().length() / 153;
				int messageSplitCount = quotient + 1;
				msgObject.setIsConcatMessageIndicator(true);
				msgObject.setSplitCount(messageSplitCount);
			}
		} else if (request.getMsgType().equalsIgnoreCase("fu")) {
			msgObject.setMessageType("FU");
			if (request.getMessageText().length() > 70) {
				int quotient = request.getMessageText().length() / 67;
				int messageSplitCount = quotient + 1;
				msgObject.setSplitCount(messageSplitCount);
				msgObject.setIsConcatMessageIndicator(true);
			}
			try {
				String hexString = Hex.encodeHexString(request.getMessageText().getBytes("UTF-16BE"));
				msgObject.setMessage(hexString);
			} catch (UnsupportedEncodingException e) {
				logger.error("Error while converting unicode message to HEX string: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			msgObject.setMessageType("UC");
			if (request.getMessageText().length() > 70) {
				int quotient = request.getMessageText().length() / 67;
				int messageSplitCount = quotient + 1;
				msgObject.setSplitCount(messageSplitCount);
				msgObject.setIsConcatMessageIndicator(true);
			}
			try {
				String hexString = Hex.encodeHexString(request.getMessageText().getBytes("UTF-16BE"));
				msgObject.setMessage(hexString);
			} catch (UnsupportedEncodingException e) {
				logger.error("Error while converting unicode message to HEX string: " + e.getMessage());
				e.printStackTrace();
			}
		}

//		msgObject.setReceiveTime(new java.sql.Timestamp(new Date().getTime()));

		// Time upto seconds
		Date currentTime = new Date(); // Get the current date and time
		long seconds = currentTime.getTime() / 1000; // Convert to seconds since epoch
		Timestamp timestamp = new Timestamp(seconds * 1000); // Create a Timestamp object
		msgObject.setReceiveTime(timestamp);
		// END
		// response.setTime(msgObject.getReceiveTime().toString());
		// msgObject.setCircleId(reqObj.getCircleId());
		// msgObject.setExpiryTime(reqObj.getExpiry());

		String requestId = messageId;
		msgObject.setUniqueId(messageId);
		msgObject.setInstanceId(messageSource);
		List<String> partMessageIds = new ArrayList<>();
		partMessageIds.add(messageId);
		if (msgObject.getSplitCount() > 1) {
			for (int i = 1; i < msgObject.getSplitCount(); i++) {
				String newPartMessageId = mNumber.substring(5, mNumber.length()) + instanceId
						+ System.currentTimeMillis() + "" + i;
				requestId = requestId + "#" + newPartMessageId;
				partMessageIds.add(newPartMessageId);
			}
			msgObject.setRequestId(requestId);
		} else {
			msgObject.setRequestId(messageId);
		}
		/*
		 * response.setPartMessageIds(partMessageIds);
		 * response.setTotalMessageParts(msgObject.getSplitCount()); if
		 * (response.getShortUrl() != null && response.getShortUrl().length() > 1) {
		 * msgObject.setIsContainsShortUrl('Y');
		 * msgObject.setLongUrl(response.getLongUrl());
		 * msgObject.setShortUrl(response.getShortUrl()); }
		 */

		if (request.getCustRef() != null) {
			msgObject.setCustRef(request.getCustRef());
		}

		/*
		 * if ((ngUser.getNgAccountType().getId() == 1) && ngUser.getIsDndCheck() ==
		 * 'N') { msgObject.setAccountType("trans"); } else if
		 * ((ngUser.getNgAccountType().getId() == 2)) {
		 * msgObject.setAccountType("promo"); } else if
		 * (ngUser.getNgAccountType().getId() == 3 && ngUser.getIsDndCheck() == 'Y') {
		 * msgObject.setAccountType("transpromo"); }
		 */

		if (request.getEntityId() != null) {
			msgObject.setEntityId(request.getEntityId());
		}

		if (request.getDltTemplateId() != null) {
			msgObject.setTemplateId(request.getDltTemplateId());
		}

		return objectMapper.writeValueAsString(msgObject);
	}

	public static void main(String[] args) {
		String n = "+91 9953 	870 879 ";
		if (n.contains(" ")) {
			System.out.println(n);
			n = n.replaceAll("\\s", "");
			System.out.println(n);
		}
	}

	/**
	 * @author Aman
	 * @param authorizationHeader
	 * @return token without "Bearer"
	 */
	private String extractBearer(String authorizationHeader) {
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			logger.info("Token will be after removing Bearer : " + authorizationHeader.substring(7));
		}
		return authorizationHeader.substring(7);
	}

}