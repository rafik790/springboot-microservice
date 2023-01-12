### Monitoring Microservices Metrics & Health inside microservices network using Micrometer, Prometheus, Grafana
---

**Description:** We have six maven projects with the names **accounts, loans, cards, configserver, eurekaserver, gatewayserver** which are continuation. All these microservices will be updated in this section to implement monitoring metrics & health inside microservices network using **micrometer, 
prometheus, grafana.**

**Key steps:**
- Open the **pom.xml** of the microservices **accounts, loans, cards** and make sure to add the below required dependencies of **micrometer,prometheus** in all of them. 
  ```xml
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
  </dependency>
  <dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
  </dependency>
  ```
- Open the **AccountsApplication.java** and create a bean of type **TimedAspect** inside it like we discussed in the course. After making the changes your 
  **AccountsApplication.java** should look like shown below,
  ### \bank-accounts\src\main\java\com\libanto\net\BankAccountsApplication.java
```java
  package com.libanto.net.bankaccounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@SpringBootApplication
@EnableFeignClients
@RefreshScope
@ComponentScans({ @ComponentScan("com.libanto.net.bankaccounts.controllers")})
@EnableJpaRepositories("com.libanto.net.bankaccounts.repository")
@EntityScan("com.libanto.net.bankaccounts.entity")
public class BankAccountsApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankAccountsApplication.class, args);
	}
	
	@Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

}
```
- Open the **AccountsController.java** and create a custom metric for **"/myAccount"** API with the help of annotation **@Timed** like we discussed in the course.After making 
  the changes your **AccountsController.java** should look like shown below,
  ### \bank-accounts\src\main\java\com\libanto\net\controller\AccountsController.java
```java
    
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
import io.micrometer.core.annotation.Timed;

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
	@Timed(value="getMyAccountDetails.time",description="Time taken to return Account Details")
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
	@Timed(value="getCustomerDetails.time",description="Time taken to return Account Details")
	@CircuitBreaker(name="getCustomerDetailsCircuitBreaker",fallbackMethod="getCustomerDetailsFallBack")
	@Retry(name="getCustomerDetailsRetry",fallbackMethod="getCustomerDetailsFallBack")
	public CustomerDetailsResp getCustomerDetails(@RequestHeader("libanto-correlation-id") String correlationid,
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
- Start the microservices in the order of **configserver, eurekaserver, accounts**. 
- Once all the required microservices are started, access the URL http://localhost:8080/myAccount/1. 
 - Open the URL http://localhost:8080/actuator/prometheus to validate if the custom metric **'getAccountDetails.time'** that we created is showing under metrics information.
- Stop all the microservices that are running inside the eclipse.
- Generate the docker images for the microservices **'accounts, loans, cards'** and push them into Docker hub by following the similar steps we discussed in the 
  previous sections.
- Like we discussed in the course, create a **prometheus.yml** inside the path and content like shown below,
  ### \docker-compose\docker-compose\monitor\prometheus.yml
```yaml
global:
  scrape_interval:     5s # Set the scrape interval to every 5 seconds.
  evaluation_interval: 5s # Evaluate rules every 5 seconds.
scrape_configs:
  - job_name: 'accounts'
    metrics_path: '/actuator/prometheus'
    static_configs:
    - targets: ['accounts:8080']
  - job_name: 'loans'
    metrics_path: '/actuator/prometheus'
    static_configs:
    - targets: ['loans:8090']
  - job_name: 'cards'
    metrics_path: '/actuator/prometheus'
    static_configs:
    - targets: ['cards:8091']
  ```
- Now in the same folder where **prometheus.yml** is present, create a **docker-compose.yml** file with the following content,
### \docker-compose\docker-compose\monitor\docker-compose.yml
```yaml
version: "3.8"

services:
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - libantobank 
  grafana:
    image: "grafana/grafana:latest"
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=12345678aA
    networks:
     - libantobank
    depends_on:
      - prometheus
      
  zipkin:
    image: openzipkin/zipkin
    mem_limit: 700m
    ports:
      - "9411:9411"
    networks:
     - libantobank
     
  configserver:
    image: rafik790/bank-configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
      - libantobank
    depends_on:
      - zipkin
    environment:
      SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
     
  eurekaserver:
    image: rafik790/bank-eurekaserver:latest
    mem_limit: 700m
    ports:
      - "8070:8070"
    networks:
      - libantobank
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
      SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      
  accounts:
    image: rafik790/bank-accounts:latest
    mem_limit: 700m
    ports:
      - "8080:8080"
    networks:
      - libantobank
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
      SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
     
  loans:
    image: rafik790/bank-loans:latest
    mem_limit: 700m
    ports:
      - "8090:8090"
    networks:
      - libantobank
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
      SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
    
  cards:
    image: rafik790/bank-cards:latest
    mem_limit: 700m
    ports:
      - "8091:8091"
    networks:
      - libantobank
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
      SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      
  gatewayserver:
    image: rafik790/bank-gatewayserver:latest
    mem_limit: 700m
    ports:
      - "8072:8072"
    networks:
      - libantobank
    depends_on:
      - configserver
      - eurekaserver
      - accounts
      - loans
      - cards
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
      SPRING_ZIPKIN_BASEURL: http://zipkin:9411/
      
networks:
  libantobank:
```
- Open the command line tool where the docker-compose.yml is present and run the docker compose command **"docker-compose up"** to start all the microservices containers with a single command. All the running containers can be validated by running a docker command **"docker ps"**.
- Open the URL http://localhost:9090/targets/ inside a browser and validate all the details, graphs present inside prometheus like we discussed in the course.
- Open the URL http://localhost:3000/login/ inside a browser and enter the login details(**admin/12345678aA**) of Grafana like we discussed in the course. Inside Grafana provide prometheus details, build custom dashboards, alerts like we discussed in the course.
- Stop all the running containers by executing the docker compose command "docker-compose down" from the location where docker-compose.yml is present.

---
### HURRAY !!! Congratulations, you successfully implemented Metrics & Health monitoring inside microservices network using Micrometer, Prometheus, Grafana
---
