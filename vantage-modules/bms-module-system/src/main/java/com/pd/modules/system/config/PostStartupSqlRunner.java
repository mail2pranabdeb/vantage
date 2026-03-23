package com.pd.modules.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Load SQL scripts after Hibernate creates schema
 */
@Configuration
public class PostStartupSqlRunner {

    private static final Logger log = LoggerFactory.getLogger(PostStartupSqlRunner.class);

    @Autowired
    private DataSource dataSource;

    @Bean
    @Order(2)
    public CommandLineRunner loadDataSql() {
        return args -> {
            log.info("=== Executing data.sql for initial data ===");
            try {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("data.sql"));
                populator.addScript(new ClassPathResource("data-pending-features.sql"));
                populator.setContinueOnError(true);
                populator.execute(dataSource);
                log.info("=== SQL scripts executed successfully ===");
            } catch (Exception e) {
                log.error("Failed to execute SQL scripts", e);
            }
        };
    }
}
