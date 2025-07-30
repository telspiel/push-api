package com.noesis.push.api.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noesis.domain.constants.ErrorCodesEnum;
import com.noesis.domain.persistence.NgCampaignInfo;
import com.noesis.domain.persistence.NgIpRestriction;
import com.noesis.domain.persistence.NgShortUrl;
import com.noesis.domain.persistence.NgShortUrlChildMapping;
import com.noesis.domain.persistence.NgUser;
import com.noesis.domain.persistence.NgUserDomainMapping;
import com.noesis.domain.persistence.NgUserSenderIdMap;
import com.noesis.domain.platform.MessageObject;
import com.noesis.domain.service.NgIpRestrictionService;
import com.noesis.domain.service.NgShortUrlChildMappingService;
import com.noesis.domain.service.ShortUrlService;
import com.noesis.domain.service.UserDomainMappingService;
import com.noesis.domain.service.UserSenderIdMapService;
import com.noesis.domain.service.UserService;
import com.noesis.push.api.domain.PushRequest;
import com.noesis.push.api.domain.PushResponse;
import com.noesis.push.api.kafka.MessageProducer;

@RestController
public class SendOneToOneMessageController {

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
	UserSenderIdMapService userSenderIdMapService;

	@Value("${push.api.kafka.topic.trans}")
	private String transTopicName;

	@Value("${push.api.kafka.topic.promo}")
	private String promoTopicName;

	@Value("${push.api.kafka.topic.transpromo}")
	private String transPromoTopicName;

	@Autowired
	private UserDomainMappingService userDomainMappingService;

	@Autowired
	private NgIpRestrictionService ngIpRestrictionService;

	@RequestMapping(value = "/sendmsg", method = { RequestMethod.GET, RequestMethod.POST,
			RequestMethod.OPTIONS }, produces = { "application/json" })
	public String uploadFile(@RequestParam(value = "username", required = false) String userName,
			@RequestParam(value = "apikey", required = false) String apiKey,
			@RequestParam(value = "dest", required = false) String mNumber,
			@RequestParam(value = "schtime", required = false) String scheduleTime,
			@RequestParam(value = "signature", required = false) String senderId,
			@RequestParam(value = "custref", required = false) String custRef,
			@RequestParam(value = "hashId", required = false) String hashId,
			@RequestParam(value = "expiry", required = false) String expiryMinutes,
			@RequestParam(value = "msgtype", required = false) String messageType,
			@RequestParam(value = "msgtxt", required = false) String messageText,
			@RequestParam(value = "domain", required = false) String userDomain,
			@RequestParam(value = "converturl", required = false) String convertUrl,
			@RequestParam(value = "campaign", required = false) String campaignName,
			@RequestParam(value = "crmurl", required = false) String callbackUrl,
			@RequestParam(value = "entityid", required = false) String entityId,
			@RequestParam(value = "templateid", required = false) String dltTemplateId, HttpServletRequest request1,
			HttpServletResponse httpServletResponse) throws JsonProcessingException, ServletException, IOException {

		// ip restriction
		String ipAddress = null;
		try {

			logger.info("check for client ip");
			logger.info("check for client ip.............117");
			ipAddress = request1.getHeader("X-Forwarded-For"); // we collect ip of client.
			logger.info("client ip from header" + ipAddress);
			// String ipAddress = request1.getRemoteAddr();
			if (ipAddress == null || ipAddress.isEmpty()) {
				ipAddress = request1.getRemoteAddr();
				logger.info("clientip is from remote  " + ipAddress);
			}
			logger.info("client ip is  " + ipAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// end ip

		logger.info("Push Request received for user: {}, destination {}", userName, mNumber);
		DateFormat df = new SimpleDateFormat("ddMMyyyy");
		if (convertUrl == null) {
			convertUrl = "Y";
		}

		// start

		List<NgIpRestriction> getclientIpByUserName = ngIpRestrictionService.getclientIpByUserName(userName);
		List<String> collect = getclientIpByUserName.stream().map(e -> e.getiP()).collect(Collectors.toList());
		boolean finalCheck = checkIp(collect, ipAddress);
		if (finalCheck == false) {
			// throw new Exception("not coorect ip ");
			logger.error("ip not whitelisted");
			httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			return "ip not whitelisted";
		}

		// end

		NgUser ngUser = userService.getUserByName(userName);
		NgUserSenderIdMap ngUserSenderIdMap = userSenderIdMapService.getSenderIdByIdAndUserId(senderId, ngUser.getId());
		String ngServiceType = null;
		if (ngUserSenderIdMap != null && ngUserSenderIdMap.getNgServiceType() != null) {
			ngServiceType = ngUserSenderIdMap.getNgServiceType().getDisplayCode();
		}
		if (mNumber.startsWith("+")) {
			mNumber = mNumber.substring(1);
		}
		PushRequest request = new PushRequest(userName, mNumber, apiKey, scheduleTime, senderId, custRef, expiryMinutes,
				messageType, messageText, userDomain, convertUrl, entityId, dltTemplateId, ngServiceType, null, hashId);
		PushResponse response = new PushResponse();

		String messageSource = "pushapi-0-" + ngUser.getUserName() + "-campaign-" + df.format(new Date());
		if (campaignName == null) {
			campaignName = ngUser.getUserName() + "-campaign-" + df.format(new Date());
		}
		boolean validateRequest = false;
		try {
			validateRequest = validateRequest(request, response, ngUser, callbackUrl, campaignName);

			if (!validateRequest) {
				return objectMapper.writeValueAsString(response);
			} else {
				response.setCampaignName(campaignName);
				processRequest(request, response, ngUser, messageSource);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return objectMapper.writeValueAsString(response);
	}

	private boolean validateRequest(PushRequest request, PushResponse response, NgUser ngUser, String callbackUrl,
			String campaignName) {
		if (ngUser == null || request.getUserName() == null || !request.getUserName().equals(ngUser.getUserName())) {
			response.setCode(ErrorCodesEnum.INVALID_USER.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_USER.getErrorDesc());
			return false;
		}

		if (request.getApiKey() == null || !request.getApiKey().equals(ngUser.getEncryptedPassword())) {
			response.setCode(ErrorCodesEnum.INVALID_API_KEY.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_API_KEY.getErrorDesc());
			return false;
		}

		

		if (request.getmNumber() == null
				|| (request.getmNumber().length() == 12 && !request.getmNumber().startsWith("91"))
				|| !Pattern.compile("\\d+").matcher(request.getmNumber()).matches()
				|| (request.getmNumber().length() == 11 && !request.getmNumber().startsWith("0"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("1"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("2"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("3"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("4"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("5"))) {
			response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
			return false;
		}

		if (request.getmNumber() == null || request.getmNumber().length() < 10
				|| request.getmNumber().trim().length() > 12
				|| !Pattern.compile("\\d+").matcher(request.getmNumber()).matches()) {
			response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
			return false;
		}

		if (request.getMessageText() == null || request.getMessageText().length() == 0) {
			response.setCode(ErrorCodesEnum.INVALID_MESSAGE_TEXT.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_MESSAGE_TEXT.getErrorDesc());
			return false;
		}

		if (!(request.getMsgType().equalsIgnoreCase("pm") || request.getMsgType().equalsIgnoreCase("uc")
				|| request.getMsgType().equalsIgnoreCase("fl"))) {
			response.setCode(ErrorCodesEnum.INVALID_MESSAGE_TYPE.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_MESSAGE_TYPE.getErrorDesc());
			return false;
		}

		if (request.getMsgType() == null
				|| request.getMsgType().equalsIgnoreCase("uc") && isPlainText(request.getMessageText())
				|| request.getMsgType().equalsIgnoreCase("pm") && isUnicodeText(request.getMessageText())) {
			response.setCode(ErrorCodesEnum.INVALID_MESSAGE_TYPE.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_MESSAGE_TYPE.getErrorDesc());
			return false;
		}

		if (request.getSignature() == null || request.getSignature().length() < 3
				|| request.getSignature().length() > 14) { // Changes Sender Id Length Replace "\\d{6}"(Aman)
			response.setCode(ErrorCodesEnum.INVALID_SENDER_ID.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_SENDER_ID.getErrorDesc());
			return false;
		}
		if (request.getMessageServiceType() != null && !request.getMessageServiceType().equals("promo")) {
			Matcher m2 = Pattern.compile("^(?![0-9]*$)[a-zA-Z0-9]+$").matcher(request.getSignature());
			if ((!m2.matches() && request.getSignature() == null) || request.getSignature().length() < 3
					|| request.getSignature().length() > 14) {
				response.setCode(ErrorCodesEnum.INVALID_SENDER_ID.getErrorCode());
				response.setDesc(ErrorCodesEnum.INVALID_SENDER_ID.getErrorDesc());
				return false;
			}
		} else if (request.getMessageServiceType() != null && request.getMessageServiceType().equals("promo")) {
			Matcher m1 = Pattern.compile("^\\d{3,14}$").matcher(request.getSignature());
			if (!(m1.matches())) {
				response.setCode(ErrorCodesEnum.INVALID_SENDER_ID.getErrorCode());
				response.setDesc(ErrorCodesEnum.INVALID_SENDER_ID.getErrorDesc());
				return false;
			}
		}

		else {
			logger.info("Message service type received others.");
			Matcher m2 = Pattern.compile("^(?![0-9]*$)[a-zA-Z0-9]+$").matcher(request.getSignature());

			if (ngUser.getIsShortCodeAllowed() == 'Y' || m2.matches()) {
				request.setMessageServiceType("trans");
				return true;
			}

			m2 = Pattern.compile("^\\d{3,14}$").matcher(request.getSignature());
			if (m2.matches()) {
				request.setMessageServiceType("promo");
				return true;
			}

			response.setCode(ErrorCodesEnum.INVALID_SENDER_ID.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_SENDER_ID.getErrorDesc());
			return false;
		}

		if (request.getCustRef() != null) {
			response.setCustRef(request.getCustRef());
		}

		if (request.getHashId() != null) {
			response.setHashId(request.getHashId());
		}

//		if ((request.getMessageText().contains("http://") || request.getMessageText().contains("https://"))
//				&& ngUser.getIsVisualizeAllowed() == 'Y' && request.getConvertUrl().equalsIgnoreCase("Y")) {
//			List<String> longUrls = extractUrls(request.getMessageText());
//			if (longUrls != null && longUrls.size() > 0) {
//				response.setLongUrl(longUrls.get(0));
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
//				logger.info("long urs in the text are {}", longUrls.toString());
//				String uniqueId = shortUrlService.shortenURL(finalHostName, longUrls.get(0), ngUser.getId(), 'Y');
//				String shortenedUrl = finalHostName + "/" + uniqueId; // messageText =
//				request.setMessageText(request.getMessageText().replace(longUrls.get(0), shortenedUrl));
//				response.setShortUrl(shortenedUrl);
//
//				NgShortUrlChildMapping ngShortUrlChildMapping = generateNgShortUrlChildMappingObject(ngUser,
//						campaignName, null, uniqueId, shortenedUrl, request.getmNumber(), null, callbackUrl);
//
//				ngShortUrlChildMappingService.saveChildShortUrlMapping(ngShortUrlChildMapping, longUrls.get(0), 'Y');
//			}
//		}

		// Changes for converting multiple Long URL into Short URL
		if ((request.getMessageText().contains("http://") || request.getMessageText().contains("https://"))
				&& ngUser.getIsVisualizeAllowed() == 'Y' && request.getConvertUrl().equalsIgnoreCase("Y")) {
			List<String> longUrls = SendOneToOneMessageController.extractUrls(request.getMessageText());
			logger.info("Found {} long URLs in the message.", longUrls.size());
			if (longUrls != null && longUrls.size() > 0) {
				for (String longUrl : longUrls) {
					logger.info("Processing long URL: {}", longUrl);
					String finalHostName = "gmly.in";
					// Convert longUrl To Short Url.
					if (request.getDomain() != null) {
						NgUserDomainMapping userDomainMapping = userDomainMappingService
								.isDomainNameActiveAndApproved(ngUser.getId(), request.getDomain(), 'Y', 'Y');
						if (userDomainMapping != null) {
							finalHostName = userDomainMapping.getDomainName();
							logger.info("Using custom domain: {}", finalHostName);
						}
					} else {
						List<NgUserDomainMapping> ngUserDomainMappingList = userDomainMappingService
								.findAllDefaultHostByPlatform();
						if (ngUserDomainMappingList != null && ngUserDomainMappingList.size() > 0) {
							finalHostName = ngUserDomainMappingList.get(0).getDomainName();
							logger.info("Using default domain: {}", finalHostName);
						}
					}
					String uniqueId = shortUrlService.shortenURL(finalHostName, longUrl, ngUser.getId(), 'Y');

					String senderId = request.getSignature();
					String shortenedUrl = finalHostName + "/" + senderId + "/" + uniqueId;
					logger.info("Shortened URL: {}", shortenedUrl);
					String custRef = request.getCustRef();
					// Replace each long URL with its short URL in the message text
					request.setMessageText(request.getMessageText().replace(longUrl, shortenedUrl));
					NgShortUrlChildMapping ngShortUrlChildMapping = SendOneToOneMessageController
							.generateNgShortUrlChildMappingObject(ngUser, campaignName, null, uniqueId, shortenedUrl,
									request.getmNumber(), null, callbackUrl, finalHostName, custRef);
					ngShortUrlChildMappingService.saveChildShortUrlMapping(ngShortUrlChildMapping, longUrl, 'Y');
					logger.info("Saved short URL mapping for: {}", longUrl);
				}
			} else {
				logger.warn("No long URLs found in the message.");
			}
		} else {
			logger.debug("Message does not require URL conversion or visualization is not allowed.");
		}
		return true;
	}

	private boolean isPlainText(String messageText) {
		// Check for plain text (non-Unicode) characters
		return messageText != null && messageText.chars().allMatch(c -> c < 128);
	}

	private boolean isUnicodeText(String messageText) {
		// Check for plain text (non-Unicode) characters
		return messageText != null && messageText.chars().anyMatch(c -> c > 127);
	}

	private void processRequest(PushRequest request, PushResponse response, NgUser ngUser1, String messageSource) {
		try {
			String messageId = MessageObject.generateMessageId(request.getmNumber(), instanceId);
			response.setReqId(messageId);

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
					String kafkaPriority = ngUser.getKafkaPriority();
					String kafkaTopic = transTopicName + kafkaPriority;
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

			response.setReqId(messageId);
			response.setCode("6001");
			response.setDesc("Message received by platform.");
		} catch (Exception e) {
			response.setCode("-10");
			response.setDesc("Internal error occured.");
			response.setReqId(null);
		}
	}

	private String generateJsonMessage(PushRequest request, NgUser ngUser, String messageId, PushResponse response,
			String messageSource) throws JsonProcessingException {
//		String mNumber = request.getmNumber();
		MessageObject msgObject = new MessageObject();
		msgObject.setSenderId(request.getSignature());
		msgObject.setMessage(request.getMessageText());
		msgObject.setMessageId(messageId);
		// msgObject.setAccountType(ngUser.getNgAccountType().getCode());
		msgObject.setMessageServiceType(request.getMessageServiceType());
		msgObject.setMessageServiceSubType(request.getMessageServiceSubType());

		msgObject.setDestNumber(request.getmNumber());
		msgObject.setUsername(ngUser.getUserName());
		msgObject.setUserId(ngUser.getId());
		msgObject.setSplitCount(1);

		String msg = request.getMessageText();

		int newLineChars = 0;
		if (msg.contains("\r\n")) {
			String tempMessage[] = msg.split("\\r\\n");
			newLineChars = tempMessage.length - 1;
			logger.info("total new line char with CR : {}", newLineChars);
		} /*
			 * else{ if(msg.contains("\n")){ String tempMessage[] = msg.split("\\n");
			 * newLineChars = tempMessage.length-1;
			 * logger.info("total new line char without CR : {}", newLineChars); } }
			 */
		logger.info("original message length : {}", request.getMessageText().length());
		int messageLength = request.getMessageText().length() - newLineChars;
		logger.info("message length after removing LF and CR : {}", messageLength);

		if (request.getMsgType().equalsIgnoreCase("pm")) {
			char[] messageCharArray = request.getMessageText().toCharArray();
			for (int i = 0; i < messageCharArray.length; i++) {
				char temp = messageCharArray[i];
				if (temp == '[' || temp == ']' || temp == '^' || temp == '{' || temp == '}' || temp == '\\'
						|| temp == '~' || temp == '|' || temp == '€') {
					logger.info("temp char is : {}", temp);
					messageLength++;
				}
			}
			logger.info("new message length with temp char : {}", messageLength);

			msgObject.setMessageType("PM");
			if (messageLength > 160) {
				int quotient = messageLength / 153;
				int messageSplitCount = quotient;
				if ((messageLength % 153) == 0) {
					messageSplitCount = quotient;
				} else {
					messageSplitCount = quotient + 1;
				}
				msgObject.setIsConcatMessageIndicator(true);
				msgObject.setSplitCount(messageSplitCount);
			}
		} else if (request.getMsgType().equalsIgnoreCase("fl")) {
			msgObject.setMessageType("FL");
			if (messageLength > 160) {
				int quotient = request.getMessageText().length() / 153;
				int messageSplitCount = quotient;
				if ((messageLength % 153) == 0) {
					messageSplitCount = quotient;
				} else {
					messageSplitCount = quotient + 1;
				}
				msgObject.setIsConcatMessageIndicator(true);
				msgObject.setSplitCount(messageSplitCount);
			}
		} else if (request.getMsgType().equalsIgnoreCase("fu")) {
			msgObject.setMessageType("FU");
			if (messageLength > 70) {
				int quotient = request.getMessageText().length() / 67;
				int messageSplitCount = quotient;
				if ((messageLength % 67) == 0) {
					messageSplitCount = quotient;
				} else {
					messageSplitCount = quotient + 1;
				}
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
			if (messageLength > 70) {
				int quotient = messageLength / 67;
				int messageSplitCount = quotient;
				if ((messageLength % 67) == 0) {
					messageSplitCount = quotient;
				} else {
					messageSplitCount = quotient + 1;
				}
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

		// msgObject.setReceiveTime(new java.sql.Timestamp(new Date().getTime()));
		// Time upto seconds
		Date currentTime = new Date(); // Get the current date and time
		long seconds = currentTime.getTime() / 1000; // Convert to seconds since epoch
		Timestamp timestamp = new Timestamp(seconds * 1000); // Create a Timestamp object
		msgObject.setReceiveTime(timestamp);
		// END
		response.setTime(msgObject.getReceiveTime().toString());
		// msgObject.setCircleId(reqObj.getCircleId());
		// msgObject.setExpiryTime(reqObj.getExpiry());

		String requestId = messageId;
		msgObject.setUniqueId(messageId);
		msgObject.setInstanceId(messageSource);
		List<String> partMessageIds = new ArrayList<>();
		partMessageIds.add(messageId);
		if (msgObject.getSplitCount() > 1) {
			for (int i = 1; i < msgObject.getSplitCount(); i++) {
				// String newPartMessageId = mNumber.substring(5, mNumber.length()) + instanceId
				// + System.currentTimeMillis() + "" + i;
				String newPartMessageId = MessageObject.generateMessageId(request.getmNumber(), instanceId) + "" + i;
				requestId = requestId + "#" + newPartMessageId;
				partMessageIds.add(newPartMessageId);
			}
			msgObject.setRequestId(requestId);
		} else {
			msgObject.setRequestId(messageId);
		}
		response.setPartMessageIds(partMessageIds);
		response.setTotalMessageParts(msgObject.getSplitCount());
		if (response.getShortUrl() != null && response.getShortUrl().length() > 1) {
			msgObject.setIsContainsShortUrl('Y');
			msgObject.setLongUrl(response.getLongUrl());
			msgObject.setShortUrl(response.getShortUrl());
		}

		if (request.getCustRef() != null) {
			msgObject.setCustRef(request.getCustRef());
		}

		if (request.getHashId() != null) {
			msgObject.setHashId(request.getHashId());
		}

		/*
		 * if ((ngUser.getNgAccountType().getId() == 1) && ngUser.getIsDndCheck() ==
		 * 'N') { msgObject.setAccountType("trans"); } else if
		 * ((ngUser.getNgAccountType().getId() == 2)) {
		 * msgObject.setAccountType("promo"); } else if
		 * (ngUser.getNgAccountType().getId() == 3 && ngUser.getIsDndCheck() == 'Y') {
		 * msgObject.setAccountType("transpromo"); }
		 */

		if (request.getEntityId() != null && request.getEntityId().length() > 0) {
			msgObject.setEntityId(request.getEntityId());
		}

		if (request.getDltTemplateId() != null && request.getDltTemplateId().length() > 0) {
			msgObject.setTemplateId(request.getDltTemplateId());
		}
		return objectMapper.writeValueAsString(msgObject);
	}

	public static void main(String[] args) throws UnsupportedEncodingException, DecoderException {
		String txt = "दूसरे राज्य में जाने के लिए पंजीकरण के तहत अपने और अपने परिवार के स्वास्थ्य की जाँच के लिए तिथि 05-05-2020 को समय 8 से 7 बजे सेवा केंद्र काठगड़ पहुंचें HTTPs://gmlu.in/12345/campaign/123123213 this is the end of url";
		/*
		 * String hexString = Hex.encodeHexString(txt.getBytes("UTF-16BE"));
		 * System.out.println(hexString); hexString = "hello"; String txt1 = new
		 * String(Hex.decodeHex(hexString.toCharArray()), "UTF-16BE");
		 * System.out.println(txt1);
		 * "Welcome to https://stackoverflow.com/ and here is another link http://www.google.com/ \n which is a great search engine"
		 */
		System.out.println(txt.length());
		String msg = "L3toL4 Escalation Alert: UHID: MM02363955, Location: ICU 1/ ICU06, Priority: Routine,Assign To:Mobile Transthoracic Echo,Closed By: 29-08-2022 16:05\r\nTeam Medanta";
		// msg = URLDecoder.decode(msg, "UTF-8");
		System.out.println(msg.length());
		System.out.println(msg);
		int newLineChars = 0;
		if (msg.contains("\r\n")) {
			String tempMessage[] = msg.split("\\r\\n");
			newLineChars = tempMessage.length;
			System.out.println("new line chars1 : " + newLineChars);
		} else {
			if (msg.contains("\n")) {
				String tempMessage[] = msg.split("\\n");
				newLineChars = tempMessage.length;
				System.out.println("new line chars : " + newLineChars);
			}
		}
		int messageLength = msg.length() - newLineChars;
		System.out.println("msg length after new line removal : " + messageLength);

		char[] messageCharArray = msg.toCharArray();
		for (int i = 0; i < messageCharArray.length; i++) {
			char temp = messageCharArray[i];
			if (temp == '[' || temp == ']' || temp == '\n' || temp == '^' || temp == '{' || temp == '}' || temp == '\\'
					|| temp == '~' || temp == '|' || temp == '€') {
				System.out.println(temp);
				messageLength++;
			}
		}
		System.out.println(msg);
		System.out.println(messageLength);

	}

	public static List<String> extractUrls(String text) {
		List<String> containedUrls = new ArrayList<String>();
//	    String urlRegex = "\\b(https?|ftp|gopher|telnet||file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);

		while (urlMatcher.find()) {
			containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
		}

		return containedUrls;
	}

	public static NgShortUrlChildMapping generateNgShortUrlChildMappingObject(NgUser ngUser, String campaignName,
			NgShortUrl ngShortUrl, String uniqueId, String shortenedUrl, String mobileNumber,
			NgCampaignInfo ngCampaignInfo, String callbackUrl, String finalHostName, String custRef) {
		NgShortUrlChildMapping ngShortUrlChildMapping = new NgShortUrlChildMapping();
		ngShortUrlChildMapping.setCampaignName(campaignName);
		ngShortUrlChildMapping.setChildShortUrl(shortenedUrl);
		ngShortUrlChildMapping.setChildUniqueKey(uniqueId);
		ngShortUrlChildMapping.setCreatedDate(new Date());
		ngShortUrlChildMapping.setMobileNumber(mobileNumber);
		ngShortUrlChildMapping.setParentShortUrl(finalHostName);
		ngShortUrlChildMapping.setParentShortUrlId(0);
		ngShortUrlChildMapping.setUpdatedDate(new Date());
		ngShortUrlChildMapping.setUserId(ngUser.getId());
		ngShortUrlChildMapping.setCampaignId(0);
		ngShortUrlChildMapping.setCallbackUrl(callbackUrl);
		ngShortUrlChildMapping.setCustRef(custRef);
		return ngShortUrlChildMapping;
	}

	// start

	public static boolean checkIp(List<String> list, String ip) {
		if ((!list.isEmpty() && list.contains(ip)) || list.isEmpty()) {
			return true;
		}
		return false;

	}

	// end

}