
# Configuration management inside microservices network using Spring Cloud Config
### Key steps:
- Go to https://start.spring.io/
- Fill all the details required to generate a configserver Spring Boot project and add dependencies Config Server,Spring Boot Actuator. Click GENERATE which will download the configserver maven project in a zip format
- Extract the downloaded maven project of configserver and import the same into Eclipse by following the steps mentioned in the course
- Visit pom.xml of configserver and make sure all the required dependencies are present in it. Add spring-boot-maven-plugin plugin details along with docker image name details inside like we discussed in the course. This extra spring-boot-maven-plugin details will help us to generate a docker image using Buildpacks easily. Finally your pom.xml should looks like shown below,

##configserver\pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.8-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.libanto</groupId>
	<artifactId>bank-configserver</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>bank-configserver</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>11</java.version>
		<spring-cloud.version>2021.0.5</spring-cloud.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-config-server</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<image>
						<name>${project.artifactId}</name>
					</image>
				</configuration>
			</plugin>
		</plugins>
	</build>
	<repositories>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>
	<pluginRepositories>
		<pluginRepository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</pluginRepository>
		<pluginRepository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</pluginRepository>
	</pluginRepositories>

</project>

```
- Open the SpringBoot main class BankConfigserverApplication.java . We can always identify the main class in a Spring Boot project by looking for the annotation @SpringBootApplication. On top of this main class, please add annotation '@EnableConfigServer'. This annotation will make your microservice to act as a Spring Cloud Config Server. After making the changes your ConfigserverApplication.java class should like below,

```java
package com.libanto.bankconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class BankConfigserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankConfigserverApplication.class, args);
	}

}

```
- Create a config folder under the path 'configserver\src\main\resources' and copy all the 9 property files related to accounts, loans and cards microservices like mentioned in the course.
- Open the application.properties inside configserver microservices and make the following entries inside it which will help in reading the properties from a given classpath location. Please note that encrypt.key is optional and can be used only in the scenarios where you want configserver to handle the encryption/decryption of the properties like we discussed in the course.
## \src\main\resources\application.properties
```property
spring.application.name=configserver
spring.profiles.active=native
spring.cloud.config.server.native.search-locations=classpath:/config
server.port=8071
encrypt.key=libantobankapplication
```
- Go to your Spring Boot main class BankConfigserverApplication.java and right click-> Run As -> Java Application. This will start your Spring Boot application successfully at port 8071 which is the port we configured inside application.properties. Your can confirm the same by looking at the console logs.
- Access the URLs like http://localhost:8071/accounts/default, http://localhost:8071/loans/dev, http://localhost:8071/cards/prod inside your browser to randomly validate the properties being exposed by Config Server for all the three microservices accounts, loans and cards.
- Stop the Config Server microservices which started at port 8071 earlier.
- Open the application.properties inside configserver microservices and make the following entries inside it which will help in reading the properties from a given file system location. Please make sure to create the configured folder/filesystem in your system and copy all the 9 property files related to accounts, loans and cards microservices like mentioned in the course.
- Create Github repository and upload all the 9 property files related to accounts, loans and cards microservices in to it like mentioned in the course. You can refer to https://github.com/rafik790/microservice-config as a sample reference.
- Open the application.properties inside configserver microservices and make the following entries inside it which will help in reading the properties from a given Github repository.
## \src\main\resources\application.properties
```property
spring.application.name=configserver

#spring.profiles.active=native
#spring.cloud.config.server.native.search-locations=classpath:/config
spring.profiles.active=git
spring.cloud.config.server.git.uri=https://github.com/rafik790/microservice-config.git
spring.cloud.config.server.git.clone-on-start=true
spring.cloud.config.server.git.default-label=main
server.port=8071
encrypt.key=libantobankapplication
```
- Go to your Spring Boot main class BankConfigserverApplication.java and right click-> Run As -> Java Application. This will start your Spring Boot application successfully at port 8071 which is the port we configured inside application.properties. Your can confirm the same by looking at the console logs.
- Access the URLs like http://localhost:8071/accounts/default, http://localhost:8071/loans/dev, http://localhost:8071/cards/prod inside your browser to randomly validate that properties are being read from configured Github location by Config Server for all the three microservices accounts, loans and cards.
- Now in order to integrate individual microservices accounts, loans and cards with configserver, please update the pom.xml files inside these microservices with <spring-cloud.version> details, spring-cloud-starter-config dependency, spring-cloud-dependencies under dependencyManagement. After making the changes, your pom.xml files should like below. 
## accounts\pom.xml
```yml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.8-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.libanto.net</groupId>
	<artifactId>bank-accounts</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>bank-accounts</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>11</java.version>
		<spring-cloud.version>2021.0.5</spring-cloud.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>
```
- Make sure all the required and associated libraries are downloaded under maven dependencies of accounts, loans, cards microservices.
- Like we discussed in the course, add @RefreshScope annotation on top of AccountsApplication.java, LoansApplication.java and CardsApplication.java. This is completely optional and can be used in the scenarios where we want to refresh the properties loaded into the given microservice with out the need of restart. In order to refresh the properties we can invoke the POST API /actuator/refresh exposed by the actuator through Postman. This refresh API will be exposed only if we configure
	
	management.endpoints.web.exposure.include=* inside application.properties like mentioned in the below step.
- Open the application.properties present inside accounts, loans, cards microservices and make sure to update the properties related to Config server details inside them. After making the changes, your application.properties files should like below. For more details please check the course videos.
## \bank-accounts\src\main\resources\application.properties
```property
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.web-allow-others=true
#spring.h2.console.settings.web-allow-others=true
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
logging.file.name = ./log/demo-app.log

server.port=8080

spring.application.name=accounts
spring.profiles.active=prod
spring.config.import=optional:configserver:http://localhost:8071/

management.endpoints.web.exposure.include=*
```

# Service Discovery & Registration inside microservices network using Spring Cloud Netflix Eureka

Description: This repository has four maven projects with the names bank-accounts, bank-loans, bank-cards, bank-configserver. A new microservices 'bank-eurekaserver' is created based on Spring Cloud Netflix Eureka which will act as a Service Discovery & Registration server. All the existing microservices bank-accounts, bank-loans, bank-cards are updated to register themself with the bank-eurekaserver during the startup and send heartbeat signals.accounts microservice is also updated to connect with loans and cards microservices using Netflix Feign client. Below are the key steps that are followed inside this section8 where we focused on set up of Eureka Server inside our microservices network.

Key steps:

- Go to https://start.spring.io/
- Fill all the details required to generate a eurekaserver Spring Boot project and add dependencies Eureka Server,Spring Boot Actuator, Config Client. Click GENERATE which will download the eurekaserver maven project in a zip format
  Extract the downloaded maven project of eurekaserver and import the same into Eclipse by following the steps mentioned in the course
- Visit pom.xml of eurekaserver and make sure all the required dependencies are present in it. Add spring-boot-maven-plugin plugin details along with docker image name details inside it like we discussed in the course. This extra spring-boot-maven-plugin details will help us to generate a docker image using Buildpacks easily. Please note if you are using a Spring Boot version of >=2.5 then mentioning ribbon to the exclusions list is not required. Finally your pom.xml should looks like shown below,
eurekaserver\pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.7.8-SNAPSHOT</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.libanto</groupId>
	<artifactId>bank-eurekaserver</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>bank-eurekaserver</name>
	<description>Demo project for Spring Boot</description>
	<properties>
		<java.version>11</java.version>
		<spring-cloud.version>2021.0.5</spring-cloud.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
			<exclusions>
				<exclusion>
					<groupId>org.springframework.cloud</groupId>
					<artifactId>spring-cloud-starter-ribbon</artifactId>
				</exclusion>
				<exclusion>
					<groupId>com.netflix.ribbon</groupId>
					<artifactId>ribbon-eureka</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<image>
						<name>${project.artifactId}</name>
					</image>
				</configuration>
			</plugin>
		</plugins>
	</build>
	<repositories>
		<repository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</repository>
		<repository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</repository>
	</repositories>
	<pluginRepositories>
		<pluginRepository>
			<id>spring-milestones</id>
			<name>Spring Milestones</name>
			<url>https://repo.spring.io/milestone</url>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
		</pluginRepository>
		<pluginRepository>
			<id>spring-snapshots</id>
			<name>Spring Snapshots</name>
			<url>https://repo.spring.io/snapshot</url>
			<releases>
				<enabled>false</enabled>
			</releases>
		</pluginRepository>
	</pluginRepositories>

</project>
```

Open the SpringBoot main class EurekaserverApplication.java . We can always identify the main class in a Spring Boot project by looking for the annotation @SpringBootApplication. On top of this main class, please add annotation '@EnableEurekaServer'. This annotation will make your microservice to act as a Spring Cloud Netflix Eureka Server. After making the changes your EurekaserverApplication.java class should like below,
\src\main\java\com\libanto\bankeurekaserver\BankEurekaserverApplication.java

```java
package com.libanto.bankeurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class BankEurekaserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankEurekaserverApplication.class, args);
	}

}
```

Open the application.properties inside eurekaserver microservices and make the following entries inside it like we discussed in the course. These entries will help in connecting to the Config Server and to disable the ribbon features. Please note if you are using a Spring Boot version of >=2.5 then providing ribbon configurations is not required. After making the changes, your application.properties should like below,

\src\main\resources\application.properties
```property
spring.application.name=eurekaserver
spring.config.import=optional:configserver:http://localhost:8071/
spring.cloud.loadbalancer.ribbon.enabled=false
```
Like we discussed in the course please make sure to create a eurekaserver.properties with the below content inside the location where your Config Server is reading the properties,
/eurekaserver.properties
```property
server.port=8070
eureka.instance.hostname=localhost
eureka.client.registerWithEureka=false
eureka.client.fetchRegistry=false
eureka.client.serviceUrl.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
```

Please make sure to start your configserver microservices
Go to your Spring Boot main class EurekaserverApplication.java and right click-> Run As -> Java Application. This will start your Spring Boot application successfully at port 8070 which is the port we configured inside eurekaserver.properties. Your can confirm the same by looking at the console logs.
Access the URL http://localhost:8070 inside your browser and make sure that you are able to access the Eureka Dashboard home page.
In order to make your accounts microservice to connect with eurekaserver, add the below dependencies inside accounts pom.xml like we discussed inside the course,
 ```xml   
    <dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
```

Open the application.properties inside accounts microservices and add the below entries inside it which will help in integrating with eurekaserver
\accounts\src\main\resources\application.properties

```property
eureka.instance.preferIpAddress = true 
eureka.client.registerWithEureka = true
eureka.client.fetchRegistry = true
eureka.client.serviceUrl.defaultZone = http://localhost:8070/eureka/

## Configuring info endpoint
info.app.name=Accounts Microservice
info.app.description=Libanto Bank Accounts Application
info.app.version=1.0.0
management.info.env.enabled = true

endpoints.shutdown.enabled=true
management.endpoint.shutdown.enabled=true
```

Go to your Spring Boot main class AccountsApplication.java and right click-> Run As -> Java Application. This will start your accounts microservice successfully at port 8080 which is the port we configured inside application.properties. Your can confirm the same by looking at the console logs.
Access the Eureka Server Dashboard URL http://localhost:8070 inside your browser and make sure that you are able to see that accounts microservice details on the Eureka Dashboard home page.
In order to make your loans, cards microservice to connect with eurekaserver, add the below dependency inside loans, cards pom.xml like we discussed inside the course,
 ```xml
    <dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
 ```   
Open the application.properties inside loans microservices and add the below entries inside it which will help in integrating with eurekaserver
\loans\src\main\resources\application.properties
```property
eureka.instance.preferIpAddress = true 
eureka.client.registerWithEureka = true
eureka.client.fetchRegistry = true
eureka.client.serviceUrl.defaultZone = http://localhost:8070/eureka/

## Configuring info endpoint
info.app.name=Loans Microservice
info.app.description=Libanto Bank Loans Application
info.app.version=1.0.0
management.info.env.enabled = true

endpoints.shutdown.enabled=true
management.endpoint.shutdown.enabled=true
```

Open the application.properties inside cards microservices and add the below entries inside it which will help in integrating with eurekaserver
\cards\src\main\resources\application.properties
```property
eureka.instance.preferIpAddress = true 
eureka.client.registerWithEureka = true
eureka.client.fetchRegistry = true
eureka.client.serviceUrl.defaultZone = http://localhost:8070/eureka/

## Configuring info endpoint
info.app.name=Cards Microservice
info.app.description=Libanto Bank Cards Application
info.app.version=1.0.0
management.info.env.enabled = true

endpoints.shutdown.enabled=true
management.endpoint.shutdown.enabled=true
```

Go to your Spring Boot main class LoansApplication.java and right click-> Run As -> Java Application. This will start your loans microservice successfully at port 8090 which is the port we configured inside application.properties. Your can confirm the same by looking at the console logs.
Go to your Spring Boot main class CardsApplication.java and right click-> Run As -> Java Application. This will start your cards microservice successfully at port 9000 which is the port we configured inside application.properties. Your can confirm the same by looking at the console logs.
Access the Eureka Server Dashboard URL http://localhost:8070 inside your browser and make sure that you are able to see that loans, cards microservices details on the Eureka Dashboard home page.

# Client Side Load balancing
In order to set up Client side load balancing using Feign client, add @EnableFeignClients annotation on top of AccountsApplication.java class which is present inside accounts microservice. Like discussed in the course, create two interfaces with the name LoansFeignClient.java,CardsFeignClient.java inside accounts microservice project. These two interfaces and the methods inside them will help to communicate with loans and cards microservices using Feign client from accounts microservice. These two interfaces should like below,

\accounts\src\main\java\com\eazybytes\accounts\service\client\LoansFeignClient.java
```java
package com.eazybytes.accounts.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.eazybytes.accounts.model.Customer;
import com.eazybytes.accounts.model.Loans;

@FeignClient("loans")
public interface LoansFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "myLoans", consumes = "application/json")
	List<Loans> getLoansDetails(@RequestBody Customer customer);
}
```

\accounts\src\main\java\com\eazybytes\accounts\service\client\CardsFeignClient.java

```java
package com.eazybytes.accounts.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.eazybytes.accounts.model.Cards;
import com.eazybytes.accounts.model.Customer;

@FeignClient("cards")
public interface CardsFeignClient {

	@RequestMapping(method = RequestMethod.POST, value = "myCards", consumes = "application/json")
	List<Cards> getCardDetails(@RequestBody Customer customer);
}

```

In order to fetch the cards and loans details using Feign client from accounts microservice, update the AccountsController.java to expose a new REST API /myCustomerDetails like we discussed inside the course. Your AccountsController.java should look like below,
\accounts\src\main\java\com\eazybytes\accounts\controller\AccountsController.java
```java
/**
 * 
 */
package com.eazybytes.accounts.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.accounts.config.AccountsServiceConfig;
import com.eazybytes.accounts.model.Accounts;
import com.eazybytes.accounts.model.Cards;
import com.eazybytes.accounts.model.Customer;
import com.eazybytes.accounts.model.CustomerDetails;
import com.eazybytes.accounts.model.Loans;
import com.eazybytes.accounts.model.Properties;
import com.eazybytes.accounts.repository.AccountsRepository;
import com.eazybytes.accounts.service.client.CardsFeignClient;
import com.eazybytes.accounts.service.client.LoansFeignClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author Eazy Bytes
 *
 */

@RestController
public class AccountsController {
	
	@Autowired
	private AccountsRepository accountsRepository;

	@Autowired
	AccountsServiceConfig accountsConfig;
	
	@Autowired
	LoansFeignClient loansFeignClient;

	@Autowired
	CardsFeignClient cardsFeignClient;
	
	@PostMapping("/myAccount")
	public Accounts getAccountDetails(@RequestBody Customer customer) {

		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		if (accounts != null) {
			return accounts;
		} else {
			return null;
		}

	}
	
	@GetMapping("/account/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(accountsConfig.getMsg(), accountsConfig.getBuildVersion(),
				accountsConfig.getMailDetails(), accountsConfig.getActiveBranches());
		String jsonStr = ow.writeValueAsString(properties);
		return jsonStr;
	}
	
	@PostMapping("/myCustomerDetails")
	public CustomerDetails myCustomerDetails(@RequestBody Customer customer) {
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId());
		List<Loans> loans = loansFeignClient.getLoansDetails(customer);
		List<Cards> cards = cardsFeignClient.getCardDetails(customer);

		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		customerDetails.setCards(cards);
		
		return customerDetails;

	}

}
```
Create the below three model classes inside accounts microservice which are needed by /myCustomerDetails REST API,
\accounts\src\main\java\com\eazybytes\accounts\model\Loans.java

```java
package com.eazybytes.accounts.model;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Loans {

	private int loanNumber;

	private int customerId;

	private Date startDt;

	private String loanType;

	private int totalLoan;

	private int amountPaid;

	private int outstandingAmount;

	private String createDt;

}
\accounts\src\main\java\com\eazybytes\accounts\model\Cards.java
package com.eazybytes.accounts.model;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Cards {

	private int cardId;

	private int customerId;

	private String cardNumber;

	private String cardType;

	private int totalLimit;

	private int amountUsed;

	private int availableAmount;

	private Date createDt;

}
\accounts\src\main\java\com\eazybytes\accounts\model\CustomerDetails.java
/**
 * 
 */
package com.eazybytes.accounts.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author EazyBytes
 *
 */
@Getter
@Setter
@ToString
public class CustomerDetails {
	
	private Accounts accounts;
	private List<Loans> loans;
	private List<Cards> cards;

}
Restart the accounts microservice and test the feign client changes done by invoking the endpoint http://localhost:8080/myCustomerDetails through Postman by passing the below request in JSON format. You should get the response from the accounts microservices which has all the details related to account, loans and cards.
{
  "customerId": 1
}

```
Generate the docker images and push them into Docker hub by following the similar steps we discussed in the previous sections.
Now write docker-compose.yml files inside accounts/docker-compose folder for each profile with the following content,
\accounts\docker-compose\default\docker-compose.yml
```yml
version: "3.8"

services:

  configserver:
    image: eazybytes/configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
     - libantobank
   
  eurekaserver:
    image: eazybytes/eurekaserver:latest
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
  
  loans:
    image: eazybytes/loans:latest
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
    
  cards:
    image: eazybytes/cards:latest
    mem_limit: 700m
    ports:
      - "9000:9000"
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
      
networks:
  libantobank:
```

\accounts\docker-compose\dev\docker-compose.yml
```yml
version: "3.8"

services:

  configserver:
    image: eazybytes/configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
     - eazybank
   
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
      
networks:
  eazybank:
```

\accounts\docker-compose\prod\docker-compose.yml

```yml
version: "3.8"

services:

  configserver:
    image: eazybytes/configserver:latest
    mem_limit: 700m
    ports:
      - "8071:8071"
    networks:
     - eazybank
   
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
      
networks:
  eazybank:
```

- Stop any running microservices inside your eclipse
- Based on the active profile that you want start the microservices, open the command line tool where the docker-compose.yml is present and run the docker compose command "docker-compose up" to start all the microservices containers with a single command. All the running containers can be validated by running a docker command "docker ps".
- To validate if individual microservices like accounts, loans & cards are able to register themselves with eurekaserver, invoke the Eureka Dashboard URL http://localhost:8070 through browser and validate the same. To test the feign client changes, invoke the endpoint http://localhost:8080/myCustomerDetails through Postman by passing the below request in JSON format. You should get the response from the accounts microservices which has all the details related to account, loans and cards.
{
    "customerId": 1
}
Stop the three running containers by executing the docker compose command "docker-compose down" from the location where docker-compose.yml is present.
