package com.pd.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.pd")
@EnableJpaRepositories(basePackages = "com.pd.modules")
public class BmsAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmsAdminApplication.class, args);
	}

}
