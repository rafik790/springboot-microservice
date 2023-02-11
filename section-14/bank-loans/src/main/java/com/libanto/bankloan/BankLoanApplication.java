package com.libanto.bankloan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@RefreshScope
@ComponentScans({ @ComponentScan("com.libanto.bankloan.controller")})
@EnableJpaRepositories("com.libanto.bankloan.repository")
@EntityScan("com.libanto.bankloan.model")
public class BankLoanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankLoanApplication.class, args);
	}

}
