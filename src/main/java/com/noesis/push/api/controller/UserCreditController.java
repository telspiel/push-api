package com.noesis.push.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noesis.domain.persistence.NgUser;
import com.noesis.domain.service.UserCreditMapService;
import com.noesis.domain.service.UserService;
import com.noesis.push.api.domain.UserCreditFormResponse;



@RestController
public class UserCreditController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	UserCreditMapService userCreditMapService;
	@Autowired
	UserService userService;
	@Autowired
	private ObjectMapper objectMapper;

	
//	@RequestMapping(value = "/getUserCredit/{userName}", method = RequestMethod.GET)
//	public Integer getUserCredit(@PathVariable String userName) {
//		logger.info("Getting user by name {}.", userName);
//		Integer availableCredit = userCreditMapService.getUserCreditByUserNameFromRedis(userName);
//		logger.info("Available user credit is : " + availableCredit);
//		return availableCredit;
//	}
	
	@RequestMapping(value = "/getUserCredit",method = { RequestMethod.OPTIONS, RequestMethod.GET, RequestMethod.POST }, produces = {
	"application/json" })
	public String getUserCredit(@RequestParam(value = "username", required = false) String userName,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "apikey", required = false) String apiKey) throws JsonProcessingException {
		logger.info("Getting user by name {}.", userName);
		UserCreditFormResponse response = new UserCreditFormResponse();
		NgUser ngUser = userService.getUserByName(userName);
		String userCredit = "";
		if (ngUser != null && userName.equals(ngUser.getUserName())) {
			try {
				System.out.println(password);
				System.out.println(apiKey);
				if((password.length()>0) || ((apiKey.length()>0))) {
					if ((password.equals(ngUser.getPassword()) || (apiKey.equals(ngUser.getEncryptedPassword())))) {
						Integer availableCredit = userCreditMapService.getUserCreditByUserNameFromRedis(userName);
						logger.info("Available user credit is : " + availableCredit);
						userCredit = String.valueOf(availableCredit);
						response.setCode(16000);
						response.setUserCredit(userCredit);
						response.setMessage("Sucess");
					}
					else {
						response.setCode(16001);
						response.setUserCredit("N/A");
						response.setMessage("Password Invalid");
					}
				}
				else{
						Integer availableCredit = userCreditMapService.getUserCreditByUserNameFromRedis(userName);
						logger.info("Available user credit is : " + availableCredit);
						userCredit = String.valueOf(availableCredit);
						response.setCode(16000);
						response.setUserCredit(userCredit);
						response.setMessage("Sucess");
					
				}	
			} catch (Exception e) {
				response.setCode(16001);
				response.setUserCredit("N/A");
				response.setMessage("Password Invalid");
			}
		}else {
			response.setCode(16001);
			response.setUserCredit("N/A");
			response.setMessage("UserName Invalid");
		}
		return objectMapper.writeValueAsString(response);
	}


}
