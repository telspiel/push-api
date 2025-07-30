package com.noesis.push.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noesis.domain.constants.ErrorCodesEnum;
import com.noesis.domain.persistence.NgUser;
import com.noesis.domain.persistence.NgUserBlackList;
import com.noesis.domain.service.UserBlackListService;
import com.noesis.domain.service.UserService;
import com.noesis.push.api.domain.BlacklistRequest;
import com.noesis.push.api.domain.BlacklistResponse;

@RestController
public class UserBlacklistController {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${push.api.instance.id}")
	private String instanceId;

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	UserBlackListService userBlackListService;

	@RequestMapping(value = "/addBlacklistNumber", method = { RequestMethod.OPTIONS, RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	public String uploadFile(@RequestParam(value = "username", required = false) String userName,
			@RequestParam(value = "apikey", required = false) String apiKey,
			@RequestParam(value = "dest", required = false) String mNumber,
			@RequestParam(value = "desc", required = false) String description) throws JsonProcessingException {

		logger.info("Blacklist Request received for user: {}, destination {}", userName, mNumber);

		NgUser ngUser = userService.getUserByName(userName);
		BlacklistRequest request = new BlacklistRequest(userName, mNumber, apiKey);
		BlacklistResponse response = new BlacklistResponse();
		boolean validateRequest = false;
		try {
			validateRequest = validateRequest(request, response, ngUser);
			if (!validateRequest) {
				return objectMapper.writeValueAsString(response);
			} else {
				ArrayList<NgUserBlackList> ngUserBlackListNumberList = new ArrayList<>();
				if (mNumber != null && mNumber.contains(",")) {
					String numberList[] = mNumber.split(",");
					for (String number : numberList) {
						if (!number.startsWith("91")) {
							number = "91" + number;
						}
						NgUserBlackList ngUserBlackList = new NgUserBlackList();
						ngUserBlackList.setCreatedDate(new Date());
						ngUserBlackList.setNgUser(ngUser);
						ngUserBlackList.setDescription(description);
						ngUserBlackList.setBlackListNumber(number);
						ngUserBlackListNumberList.add(ngUserBlackList);
					}
				} else {
					if (!mNumber.startsWith("91")) {
						mNumber = "91" + mNumber;
					}
					NgUserBlackList ngUserBlackList = new NgUserBlackList();
					ngUserBlackList.setCreatedDate(new Date());
					ngUserBlackList.setNgUser(ngUser);
					ngUserBlackList.setDescription(description);
					ngUserBlackList.setBlackListNumber(mNumber);
					ngUserBlackListNumberList.add(ngUserBlackList);
				}
				userBlackListService.addBulkNumberInUserBlackList(ngUserBlackListNumberList, ngUser);
				userBlackListService.loadAllUserBlackListDataInCache();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setCode("200");
		response.setDesc("success");
		response.setTime("" + System.currentTimeMillis());
		return objectMapper.writeValueAsString(response);
	}

	private boolean validateRequest(BlacklistRequest request, BlacklistResponse response, NgUser ngUser) {
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

		if (request.getmNumber() == null) {
			response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode());
			response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
			return false;
		} else if (request.getmNumber().contains(",")) {
			String numbers[] = request.getmNumber().split(",");
			for (String number : numbers) {
				if ((number.length() == 12 && !number.startsWith("91"))
						|| !Pattern.compile("\\d+").matcher(number).matches()
						|| (number.length() == 11 && !number.startsWith("0"))
						|| (number.length() == 10 && number.startsWith("1"))
						|| (number.length() == 10 && number.startsWith("2"))
						|| (number.length() == 10 && number.startsWith("3"))
						|| (number.length() == 10 && number.startsWith("4"))
						|| (number.length() == 10 && number.startsWith("5"))) {
					response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode() + " -" + number);
					response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
					return false;
				}
				
				if (number.length() < 10 || number.trim().length() > 12
						|| !Pattern.compile("\\d+").matcher(number).matches()) {
					response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode() + " -"+ number);
					response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
					return false;
				}
			}
		} else if ((request.getmNumber().length() == 12 && !request.getmNumber().startsWith("91"))
				|| !Pattern.compile("\\d+").matcher(request.getmNumber()).matches()
				|| (request.getmNumber().length() == 11 && !request.getmNumber().startsWith("0"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("1"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("2"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("3"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("4"))
				|| (request.getmNumber().length() == 10 && request.getmNumber().startsWith("5"))) {
			response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode() + " -" + request.getmNumber());
			response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
			return false;
		}else if (request.getmNumber().length() < 10 || request.getmNumber().trim().length() > 12
				|| !Pattern.compile("\\d+").matcher(request.getmNumber()).matches()) {
			response.setCode(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorCode() + " -"+request.getmNumber());
			response.setDesc(ErrorCodesEnum.INVALID_DESTINATION_NUMBER.getErrorDesc());
			return false;
		}
		return true;
	}

	@RequestMapping(value = "/removeBlacklistNumber", method = { RequestMethod.OPTIONS, RequestMethod.GET,
			RequestMethod.POST }, produces = { "application/json" })
	public String removeBlacklistNumber(@RequestParam(value = "username", required = false) String userName,
			@RequestParam(value = "apikey", required = false) String apiKey,
			@RequestParam(value = "dest", required = false) String mNumber,
			@RequestParam(value = "desc", required = false) String description) throws JsonProcessingException {

		logger.info("Blacklist Request received for user: {}, destination {}", userName, mNumber);

		NgUser ngUser = userService.getUserByName(userName);
		BlacklistRequest request = new BlacklistRequest(userName, mNumber, apiKey);
		BlacklistResponse response = new BlacklistResponse();
		boolean validateRequest = false;
		try {
			validateRequest = validateRequest(request, response, ngUser);
			if (!validateRequest) {
				return objectMapper.writeValueAsString(response);
			} else {
				ArrayList<String> ngUserBlackListNumberList = new ArrayList<>();
				if (mNumber != null && mNumber.contains(",")) {
					String numberList[] = mNumber.split(",");
					for (String number : numberList) {
						if (!number.startsWith("91")) {
							number = "91" + number;
						}
						ngUserBlackListNumberList.add(number);
					}
				} else {
					if (!mNumber.startsWith("91")) {
						mNumber = "91" + mNumber;
					}
					ngUserBlackListNumberList.add(mNumber);
				}
				userBlackListService.removeUserBlackListNumbers(ngUser, ngUserBlackListNumberList);
				userBlackListService.loadAllUserBlackListDataInCache();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setCode("200");
		response.setDesc("success");
		response.setTime("" + System.currentTimeMillis());
		return objectMapper.writeValueAsString(response);
	}
}
