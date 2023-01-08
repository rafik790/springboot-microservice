package com.libanto.net.bankaccounts.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.libanto.net.bankaccounts.config.AccountServiceConfig;
import com.libanto.net.bankaccounts.entity.Account;
import com.libanto.net.bankaccounts.entity.Properties;
import com.libanto.net.bankaccounts.repository.AccountRepository;


@RestController
public class AccountsController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountsController.class);
	
	@Autowired
	private AccountRepository accountsRepository;
	
	@Autowired
	AccountServiceConfig accountConfig;
	
	@GetMapping("/api/myAccount/{customerId}")
	public Account getMyAccountDetails(@PathVariable(value = "customerId") int customerId) {
		LOGGER.info("Customer ID: {}", customerId);
		Account account = accountsRepository.findByCustomerId(customerId);
		LOGGER.info("Accoumt Number::{}", account.getAccountNumber());
		return account;
		
	}
	

	@GetMapping("/api/account/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountConfig.getMsg(), accountConfig.getBuildVersion(),
				accountConfig.getMailDetails(), accountConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
}
