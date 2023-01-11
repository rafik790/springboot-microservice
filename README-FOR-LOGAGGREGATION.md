### Implementing Distributed tracing & Log Aggregation inside microservices network using Spring Cloud Sleuth/micrometer, Zipkin
---

**Description:** This repository has six maven projects with the names **accounts, loans, cards, configserver, eurekaserver, gatewayserver** which are continuation from the section10 repository. All these microservices will be updated in this section to implement distributed tracing & log aggregation inside microservices network using **Spring Cloud Sleuth/micrometer, Zipkin**. Below are the key steps that are followed inside this **section11** where we focused on set up of **Distributed tracing & Log Aggregation** inside our microservices network.

**Key steps:**
- Open the **pom.xml** of all the microservices **accounts, loans, cards, configserver, eurekaserver, gatewayserver** and make sure to add the below required dependency of **Spring Cloud Sleuth** in all of them. But please note this will work only if you are using Spring Cloud version which is less
than 2022.0.0
```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```
- From Spring Cloud 2022.0.0 & Spring Boot 3, the Sleuth project has been removed & the core of this project has moved to Micrometer Tracing. For more details, please refer GitHub & micrometer website (https://micrometer.io/docs/tracing). In case if you are using micrometer, please add the below maven dependencies inside pom.xml of all the projects
```xml
<dependency>
   <groupId>io.micrometer</groupId>
   <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
   <groupId>io.micrometer</groupId>
   <artifactId>micrometer-tracing</artifactId>
</dependency>
<dependency>
   <groupId>io.micrometer</groupId>
   <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
   <groupId>io.micrometer</groupId>
   <artifactId>context-propagation</artifactId>
   <version>1.0.0</version>
</dependency>
```
- Open the **AccountsController.java, LoansController.java , CardsController.java** and add the logger statements like we discussed in the course. This logger statements will help us to understand and validate how **Spring Cloud Sleuth/Micrometer** is going to add **App name, Trace ID, Span ID** information to the loggers inside the microservices. After making the changes your **AccountsController.java, LoansController.java , CardsController.java** should look like shown below,
  
### \bank-accounts\src\main\java\com\libanto\net\bankaccounts\controllers\AccountsController.java
  
```java
  /**
 * 
 */
package com.libanto.net.bankaccounts.controllers;

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
import com.libanto.net.bankaccounts.config.AccountServiceConfig;
import com.libanto.net.bankaccounts.entity.Account;
import com.libanto.net.bankaccounts.entity.Properties;
import com.libanto.net.bankaccounts.repository.AccountRepository;
import com.libanto.net.bankaccounts.resp.CardResp;
import com.libanto.net.bankaccounts.resp.CustomerDetailsResp;
import com.libanto.net.bankaccounts.resp.LoanResp;
import com.libanto.net.bankaccounts.service.client.CardsFeignClient;
import com.libanto.net.bankaccounts.service.client.LoansFeignClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;


@RestController
public class AccountsController {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountsController.class);
	
	@Autowired
	private AccountRepository accountsRepository;
	
	@Autowired
	AccountServiceConfig accountConfig;
	
	@Autowired
	LoansFeignClient loansFeignClient;

	@Autowired
	CardsFeignClient cardsFeignClient;
	
	
	@GetMapping("/myAccount/{customerId}")
	public Account getMyAccountDetails(@PathVariable(value = "customerId") int customerId) {
		LOGGER.info("Customer ID: {}", customerId);
		Account account = accountsRepository.findByCustomerId(customerId);
		LOGGER.info("Accoumt Number::{}", account.getAccountNumber());
		return account;
		
	}
	

	@GetMapping("/account/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountConfig.getMsg(), accountConfig.getBuildVersion(),
				accountConfig.getMailDetails(), accountConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
	
	
	@GetMapping("/getCustomerDetails/{customerId}")
	@CircuitBreaker(name="getCustomerDetailsCircuitBreaker",fallbackMethod="getCustomerDetailsFallBack")
	@Retry(name="getCustomerDetailsRetry",fallbackMethod="getCustomerDetailsFallBack")
	public CustomerDetailsResp myCustomerDetails(@RequestHeader("libanto-correlation-id") String correlationid,
			@PathVariable(value = "customerId") int customerId) {
		LOGGER.info("correlation-id: {}", correlationid);
		Account accounts = accountsRepository.findByCustomerId(customerId);
		List<LoanResp> loans = loansFeignClient.getMyLoans(correlationid,customerId);
		List<CardResp> cards = cardsFeignClient.getMyCards(correlationid,customerId);

		CustomerDetailsResp customerDetails = new CustomerDetailsResp();
		customerDetails.setAccount(accounts);
		customerDetails.setLoans(loans);
		customerDetails.setCards(cards);
		
		return customerDetails;

	}
	
	@SuppressWarnings("unused")
	private CustomerDetailsResp getCustomerDetailsFallBack(String correlationid,int customerId, Throwable t) {
		Account accounts = accountsRepository.findByCustomerId(customerId);
		List<LoanResp> loans = loansFeignClient.getMyLoans(correlationid,customerId);
		CustomerDetailsResp customerDetails = new CustomerDetailsResp();
		customerDetails.setAccount(accounts);
		customerDetails.setLoans(loans);
		return customerDetails;

	}
	
	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallback")
	public String sayHello() {
		return "Hello, Welcome to LibantoBank";
	}

	@SuppressWarnings("unused")
	private String sayHelloFallback(Throwable t) {
		return "Hi, You exceed rate limit ";
	}
	
	
}

```

- Start all the microservices in the order of **configserver, eurekaserver, accounts, loans, cards, gatewayserver**.
- Once all the microservices are started, access the URL http://localhost:8072/api/accounts/getCustomerDetails/1 through Postman by passing the below request in JSON format. 
  You should be able to see the logger statements along with **App name, Trace ID, Span ID** like we discussed in the course.
 
 ## Zipkin
 
 - Now in order to use distributed tracing using **Zipkin**, run the docker command **'docker run -d -p 9411:9411 openzipkin/zipkin'**. This docker command will start the zipkin docker container using the provided docker image.
 - To validate if the zipkin server started successfully or not, visit the URL http://localhost:9411/zipkin inside your browser. You should be able to see the zipkin home page.
 - Stop all the microservices that are previously started in order to update them with zipkin related changes.
 - Open the **pom.xml** of all the microservices **accounts, loans, cards, configserver, eurekaserver, gatewayserver** and make sure to add the below required dependency of **Zipkin** in all of them if you are using Spring Cloud Sleuth. 
  ```xml
   <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
   </dependency>
  ```
- Otherwise, if you are using micrometer, then please add below required dependency of **Zipkin**
  ```xml
   <dependency>
	<groupId>io.zipkin.reporter2</groupId>
	<artifactId>zipkin-reporter-brave</artifactId>
   </dependency>
  ```
- Open the **application.properties** of all the microservices **accounts, loans, cards, configserver, eurekaserver, gatewayserver** and make sure to add the below properties/configurations in all of them based on if you are using Sleuth or Micrometer.
  ```
  # Micrometer related properties
  management.tracing.sampling.probability=1.0
  management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
  management.metrics.distribution.percentiles-histogram.http.server.requests=true
  logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]

  # Sleuth related properties
  spring.sleuth.sampler.percentage=1
  spring.zipkin.baseUrl=http://localhost:9411/

  ```
- Start all the microservices in the order of **configserver, eurekaserver, accounts, loans, cards, gatewayserver**.
- Once all the microservices are started, access the URL http://localhost:8072/api/accounts/getCustomerDetails/1 through Postman by passing the below request in JSON format. 
  You should be able to see the tracing details inside zipkin console like we discussed in the course.
  
- Stop all the microservices that are previously started in order to update them with Rabbit MQ related changes.
- Now in order to push all the loggers into Rabbit MQ asynchronously, open the **pom.xml** of all the microservices **accounts, loans, cards, configserver, eurekaserver, gatewayserver** and make sure to add the below required dependency of **Rabbit MQ** in all of them. But please note that 
this will work only with Sleuth. In case if you are using micrometer, it doesn't support pushing loggers into Rabbit MQ asynchronously as of now.
  ```xml
   <dependency>	
	<groupId>org.springframework.amqp</groupId>
	<artifactId>spring-rabbit</artifactId>
   </dependency>
  ```
- Now in order to setup a Rabbit MQ server, run the docker command **'docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management'**. This docker command will start the Rabbit MQ related docker container using the provided docker image.
 - To validate if the Rabbit MQ server started successfully or not, visit the URL http://localhost:15672 inside your browser and login with username/password as **guest**
   like we discussed in the course. Please validate all the Connections, Channels, Queues etc. are empty.
 - Like we discussed in the course, create a new queue with the name **zipkin**.
 - Open the **application.properties** of all the microservices **accounts, loans, cards, configserver, eurekaserver, gatewayserver** and make sure to add the below 
  properties/configurations in all of them.
  ```
  spring.zipkin.sender.type=rabbit
  spring.zipkin.rabbitmq.queue=zipkin
  spring.rabbitmq.host=localhost
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=guest
  spring.rabbitmq.password=guest
  ```
- Start all the microservices in the order of **configserver, eurekaserver, accounts, loans, cards, gatewayserver**.
- Once all the microservices are started, access the URL http://localhost:8072/accounts/myCusomerDetails through Postman by passing the below request in JSON format. 
  You should be able to see the tracing details inside Rabbit MQ console like we discussed in the course.
  ```json
  {
    "customerId": 1
  }
  ```
- Stop all the microservices that are running inside the eclipse.
- Before generating the docker images for all the microservices, comment all the changes related to Rabbit MQ inside all the microservices like we discussed in the course.
- Generate the docker images for all the microservices and push them into Docker hub by following the similar steps we discussed in the previous sections.
- Now write docker-compose.yml files inside accounts/docker-compose folder for each profile with the following content,
### \accounts\docker-compose\default\docker-compose.yml
```yaml
version: "3.8"

services:

  zipkin:
    image: openzipkin/zipkin
    mem_limit: 700m
    ports:
      - "9411:9411"
    networks:
     - eazybank
  
  configserver:
    image: eazybytes/configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
     - eazybank
    depends_on:
      - zipkin
    environment:
      SPRING_PROFILES_ACTIVE: default
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
   
  eurekaserver:
    image: eazybytes/eurekaserver:latest
    mem_limit: 700m
    ports:
      - "8070:8070"
    networks:
     - eazybank
    depends_on:
      - configserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 15s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: default
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
      
  accounts:
    image: eazybytes/accounts:latest
    mem_limit: 700m
    ports:
      - "8080:8080"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: default
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
  
  loans:
    image: eazybytes/loans:latest
    mem_limit: 700m
    ports:
      - "8090:8090"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: default
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    
  cards:
    image: eazybytes/cards:latest
    mem_limit: 700m
    ports:
      - "9000:9000"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: default
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
   
  gatewayserver:
    image: eazybytes/gatewayserver:latest
    mem_limit: 700m
    ports:
      - "8072:8072"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
      - cards
      - loans
      - accounts
    deploy:
      restart_policy:
        condition: on-failure
        delay: 45s
        max_attempts: 3
        window: 180s
    environment:
      SPRING_PROFILES_ACTIVE: default
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
      
networks:
  eazybank:
```
### \accounts\docker-compose\dev\docker-compose.yml
```yaml
version: "3.8"

services:

  zipkin:
    image: openzipkin/zipkin
    mem_limit: 700m
    ports:
      - "9411:9411"
    networks:
     - eazybank
     
  configserver:
    image: eazybytes/configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
     - eazybank
    depends_on:
      - zipkin
    environment:
      SPRING_PROFILES_ACTIVE: dev
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
   
  eurekaserver:
    image: eazybytes/eurekaserver:latest
    mem_limit: 700m
    ports:
      - "8070:8070"
    networks:
     - eazybank
    depends_on:
      - configserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 15s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
      
  accounts:
    image: eazybytes/accounts:latest
    mem_limit: 700m
    ports:
      - "8080:8080"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
  
  loans:
    image: eazybytes/loans:latest
    mem_limit: 700m
    ports:
      - "8090:8090"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    
  cards:
    image: eazybytes/cards:latest
    mem_limit: 700m
    ports:
      - "9000:9000"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
  
  gatewayserver:
    image: eazybytes/gatewayserver:latest
    mem_limit: 700m
    ports:
      - "8072:8072"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
      - cards
      - loans
      - accounts
    deploy:
      restart_policy:
        condition: on-failure
        delay: 45s
        max_attempts: 3
        window: 180s
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    
networks:
  eazybank:
```
### \accounts\docker-compose\prod\docker-compose.yml
```yaml
version: "3.8"

services:

  zipkin:
    image: openzipkin/zipkin
    mem_limit: 700m
    ports:
      - "9411:9411"
    networks:
     - eazybank
     
  configserver:
    image: eazybytes/configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
     - eazybank
    depends_on:
      - zipkin
    environment:
      SPRING_PROFILES_ACTIVE: prod
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
   
  eurekaserver:
    image: eazybytes/eurekaserver:latest
    mem_limit: 700m
    ports:
      - "8070:8070"
    networks:
     - eazybank
    depends_on:
      - configserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 15s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
      
  accounts:
    image: eazybytes/accounts:latest
    mem_limit: 700m
    ports:
      - "8080:8080"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
  
  loans:
    image: eazybytes/loans:latest
    mem_limit: 700m
    ports:
      - "8090:8090"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    
  cards:
    image: eazybytes/cards:latest
    mem_limit: 700m
    ports:
      - "9000:9000"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
    deploy:
      restart_policy:
        condition: on-failure
        delay: 30s
        max_attempts: 3
        window: 120s
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
  
  gatewayserver:
    image: eazybytes/gatewayserver:latest
    mem_limit: 700m
    ports:
      - "8072:8072"
    networks:
      - eazybank
    depends_on:
      - configserver
      - eurekaserver
      - cards
      - loans
      - accounts
    deploy:
      restart_policy:
        condition: on-failure
        delay: 45s
        max_attempts: 3
        window: 180s
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_CONFIG_IMPORT: configserver:http://configserver:8071/
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eurekaserver:8070/eureka/
      # SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
      
networks:
  eazybank:
```
- Based on the active profile that you want start the microservices, open the command line tool where the docker-compose.yml is present and run the docker compose command **"docker-compose up"** to start all the microservices containers with a single command. All the running containers can be validated by running a docker command **"docker ps"**.
- To test the distributed tracing changes along with log aggregation, access the URL http://localhost:8072/accounts/myCusomerDetails through Postman by passing the below request in JSON format. You should be able to see the tracing details inside zipkin console like we discussed in the course.
  ```json
  {
    "customerId": 1
  }
  ```
- Stop all the running containers by executing the docker compose command "docker-compose down" from the location where docker-compose.yml is present.

---
### HURRAY !!! Congratulations, you successfully implemented Distributed tracing & Log Aggregation inside microservices network using Spring Cloud Sleuth/Micrometer, Zipkin
---
