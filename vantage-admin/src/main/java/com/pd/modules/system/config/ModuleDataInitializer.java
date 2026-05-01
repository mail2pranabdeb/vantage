package com.pd.modules.system.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Module-based database initialization with flags
 */
@Configuration
public class ModuleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ModuleDataInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Value("${init-on-fresh-db:false}")
    private boolean initOnFreshDb;

    @Bean
    @Order(1)  // Run AFTER Hibernate creates tables
    public CommandLineRunner initializeModuleData() {
        return args -> {
            log.info("=== Data Initialization Started ===");

            // Wait for Hibernate to create tables
            log.info("=== Waiting 3 seconds for Hibernate ===");
            Thread.sleep(3000);

            // Initialize data (users, roles, menus, etc.) - only if enabled and no admin user
            if (initOnFreshDb && !hasAdminUser()) {
                log.info("=== No admin user - Running data.sql ===");
                runScript("data.sql");
            } else if (initOnFreshDb) {
                log.info("=== Admin user exists - Skipping data.sql ===");
            } else {
                log.info("=== Init disabled or admin exists - Skipping ===");
            }

            log.info("=== Data Initialization Completed ===");
        };
    }

    private void runScript(String scriptName) {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource(scriptName));
            populator.setContinueOnError(true);
            populator.setSeparator(";");
            populator.execute(dataSource);
            log.info("Executed script: {}", scriptName);
        } catch (Exception e) {
            log.error("Failed to execute script: {}", scriptName, e);
        }
    }

    private boolean hasAdminUser() {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM sys_user WHERE login_name='admin'")) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
