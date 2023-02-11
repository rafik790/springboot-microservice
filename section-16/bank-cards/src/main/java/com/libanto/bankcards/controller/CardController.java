package com.libanto.bankcards.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.libanto.bankcards.config.CardServiceConfig;
import com.libanto.bankcards.model.Card;
import com.libanto.bankcards.model.Properties;
import com.libanto.bankcards.repository.CardRepository;



@RestController
public class CardController {
	private static final Logger LOGGER = LoggerFactory.getLogger(CardController.class);
	
	@Autowired
	private CardRepository cardsRepository;
	
	@Autowired
	private CardServiceConfig cardService;

	@GetMapping("/myCards/{customerId}")
	public List<Card> getCardDetails(@RequestHeader("libanto-correlation-id") String correlationid,@PathVariable(value = "customerId") int customerId) {
		LOGGER.info("Custmer ID::{}",customerId);
		LOGGER.info("Correlation ID::{}",correlationid);
		
		List<Card> cards = cardsRepository.findByCustomerId(customerId);
		if (cards != null) {
			LOGGER.info("Card Cont::{}",cards.size());
			return cards;
		} else {
			return null;
		}

	}
	
	@GetMapping("/card/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(cardService.getMsg(), cardService.getBuildVersion(),
				cardService.getMailDetails(), cardService.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
}
