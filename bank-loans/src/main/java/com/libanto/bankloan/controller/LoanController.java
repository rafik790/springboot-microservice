package com.libanto.bankloan.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.libanto.bankloan.config.LoanServiceConfig;
import com.libanto.bankloan.model.Loan;
import com.libanto.bankloan.model.Properties;
import com.libanto.bankloan.repository.LoanRepository;


@RestController
public class LoanController {
	private static final Logger LOGGER = LoggerFactory.getLogger(LoanController.class);
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	LoanServiceConfig loansConfig;
	
	@GetMapping("/myLoans/{customerId}")
	public ResponseEntity<List<Loan>> getLoansDetails(@RequestHeader("libanto-correlation-id") String correlationid,@PathVariable(value = "customerId") int customerId) {
		LOGGER.info("Custmer ID::{}",customerId);
		LOGGER.info("Correlation ID::{}",correlationid);
		List<Loan> loans = loanRepository.findByCustomerIdOrderByStartDtDesc(customerId);
		LOGGER.info("Loan Count::{}",loans.size());
		return ResponseEntity.ok().body(loans);
	}
	
	@GetMapping("/loans/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(loansConfig.getMsg(), loansConfig.getBuildVersion(),
				loansConfig.getMailDetails(), loansConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
}
