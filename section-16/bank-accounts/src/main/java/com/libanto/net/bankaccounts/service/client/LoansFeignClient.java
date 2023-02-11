package com.libanto.net.bankaccounts.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.libanto.net.bankaccounts.resp.LoanResp;

@FeignClient("loans")
public interface LoansFeignClient {
	@RequestMapping(method = RequestMethod.GET, value = "/myLoans/{customerId}", consumes = "application/json")
	List<LoanResp> getMyLoans(@RequestHeader("libanto-correlation-id") String correlationid,@PathVariable(value = "customerId") int customerId);
	
}
