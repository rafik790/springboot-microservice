# Securing Microservices using OAuth2 client credentials grant flow
**Description:** This repository has six maven projects with the names accounts, loans, cards, configserver, eurekaserver, gatewayserver. All these microservices will be leveraged to explore the securing microservices using OAuth2 client credentials grant flow. Below are the key steps that we will be following in this section17 repository,
### Key steps:
- Install & setup the Keycloak using docker command in your local system.
- Register a client inside Keycloak that supports Client Credentials grant flow.
```properties
docker run -p 7080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:20.0.3 start-dev
GetAPIEndPoint=http://localhost:7080/realms/master/.well-known/openid-configuration

Get Toke APIs:
http://localhost:7080/realms/master/protocol/openid-connect/token
Method: Post
client_id:
client_secret:
scope: openid
grant_type: client_credentials

Run zipkin:
docker run -p 9411:9411 openzipkin/zipkin

```
- Pass your client details which is created in the previous step as a request inside Postman & make sure to get an access token from the keycloak.
- Open the pom.xml of the microservices gatewayserver and make sure to add the below required dependencies of Spring Security,OAuth2
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```
- In order to make our Spring Cloud Gateway to act as a Resource server & handle both Authentication & Authorization, please create the classes SecurityConfig.java, KeycloakRoleConverter.java. They should look like below,
### SecurityConfig.java
```java
package com.libanto.bankgatewayserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http.authorizeExchange(exchanges -> exchanges.pathMatchers("/api/accounts/**").hasRole("ACCOUNTS")
				.pathMatchers("/api/cards/**").authenticated()
				.pathMatchers("/api/loans/**").permitAll())
				.oauth2ResourceServer().jwt().jwtAuthenticationConverter(grantedAuthoritiesExtractor());
		http.csrf().disable();
		return http.build();
	}

	Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
		return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
	}
}

```

### KeycloakRoleConverter.java
```java
package com.libanto.bankgatewayserver.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

	@SuppressWarnings("unchecked")
	@Override
	public Collection<GrantedAuthority> convert(Jwt jwt) {
		Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
		if (realmAccess == null || realmAccess.isEmpty()) {
			return new ArrayList<>();
		}

		Collection<GrantedAuthority> returnValue = ((List<String>) realmAccess.get("roles")).stream()
				.map(roleName -> "ROLE_" + roleName).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		return returnValue;
	}

}
```

- Open the **application.properties** inside **gatewayserver** microservices and add the following entry. Here we are providing the Keycloak URI where my resource server can validate the access tokens received.

```properties
spring.security.oauth2.resourceserver.jwt.jwk-set-uri = http://localhost:7080/realms/master/protocol/openid-connect/certs
```

- Please make sure to start all your microservices including Zipkin, Keycloak in the order
- Access the URL http://localhost:8072/api/accounts/sayHello through browser and you can expect the 401 response as the accounts API paths are secured.
- Access the URL http://localhost:8072/api/loans/loans/properties through browser and you can expect a succesfull response as there is no security for loans API paths.
- Now get an access token from keycloak & pass the same to Gateway server while trying to access the secured APIs. You can expect a successfull response.
- Once the local testing is completed successfully, generate the latest docker image for gatewayserver microservice and push into docker hub.
- Install Keycloak Auth server inside K8s cluster using the below commands,
```yml
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install keycloak bitnami/keycloak
helm install keycloak --set auth.adminPassword=12345678a my-repo/keycloak
```
- Update the Helm charts
- Deploy all the microservices using Helm charts and test.
- tlast, make sure to test Authorization changes as well by creating a new role **ACCOUNTS** inside Keycloak

# HURRAY !!! Congratulations, you successfully secured your microservices using the OAuth2 client credentials grant flow