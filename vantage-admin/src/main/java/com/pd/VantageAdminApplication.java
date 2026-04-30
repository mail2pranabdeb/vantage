package com.pd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.modulith.Modulith;

/**
 * Main application entry point.
 */
@SpringBootApplication
@Modulith
@EnableJpaRepositories
@EnableJpaAuditing
@EnableAspectJAutoProxy
@EnableAsync
public class VantageAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(VantageAdminApplication.class, args);
    }

}
