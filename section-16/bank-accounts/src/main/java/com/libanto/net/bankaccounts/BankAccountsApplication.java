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
