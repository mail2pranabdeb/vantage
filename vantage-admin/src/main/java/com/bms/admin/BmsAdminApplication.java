package com.pd.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.pd")
public class BmsAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(BmsAdminApplication.class, args);
	}

}
