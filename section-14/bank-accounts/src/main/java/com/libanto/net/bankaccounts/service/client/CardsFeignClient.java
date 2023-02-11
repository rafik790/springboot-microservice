package com.libanto.net.bankaccounts.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.libanto.net.bankaccounts.resp.CardResp;

@FeignClient("cards")
public interface CardsFeignClient {
	@RequestMapping(method = RequestMethod.GET, value = "/myCards/{customerId}", consumes = "application/json")
	List<CardResp> getMyCards(@RequestHeader("libanto-correlation-id") String correlationid,@PathVariable(value = "customerId") int customerId);
}

