package com.pd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application entry point.
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
@EnableAspectJAutoProxy  // Enable AOP aspects for operation logging
@EnableAsync  // Enable async event listeners
public class VantageAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(VantageAdminApplication.class, args);
	}

}
