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

    @Value("${module.init.system.enabled:true}")
    private boolean systemModuleEnabled;

    @Value("${module.init-on-fresh-db:true}")
    private boolean initOnFreshDb;

    @Bean
    @Order(0)  // Run BEFORE Hibernate
    public CommandLineRunner initializeSchema() {
        return args -> {
            log.info("=== Schema Initialization Started ===");
            log.info("Init on Fresh DB: {}", initOnFreshDb);
            
            if (!initOnFreshDb) {
                log.info("=== Skipping schema initialization ===");
                return;
            }

            // Run schema.sql FIRST to create all tables
            log.info("=== Running schema.sql to create all tables ===");
            runScript("schema.sql");
            log.info("=== Schema initialization completed ===");
        };
    }

    @Bean
    @Order(1)  // Run AFTER Hibernate
    public CommandLineRunner initializeModuleData() {
        return args -> {
            log.info("=== Data Initialization Started ===");

            // Wait for Hibernate to enhance tables
            log.info("=== Waiting 3 seconds for Hibernate ===");
            Thread.sleep(3000);

            // Initialize data (users, roles, menus, etc.)
            if (initOnFreshDb && !hasAdminUser()) {
                log.info("=== No admin user - Running data.sql ===");
                runScript("data.sql");
            } else if (initOnFreshDb) {
                log.info("=== Admin user exists - Skipping data.sql ===");
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

    private boolean menuExists(Long menuId) {
        try (Connection conn = dataSource.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_id=" + menuId)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
