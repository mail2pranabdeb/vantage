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
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Module-based database initialization with flags
 * Each module can be enabled/disabled via application.yml
 */
@Configuration
public class ModuleDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ModuleDataInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Value("${module.init.system.enabled:true}")
    private boolean systemModuleEnabled;

    @Value("${module.init.quartz.enabled:true}")
    private boolean quartzModuleEnabled;

    @Value("${module.init.generator.enabled:true}")
    private boolean generatorModuleEnabled;

    @Bean
    @Order(1)
    public CommandLineRunner initializeModuleData() {
        return args -> {
            log.info("=== Module Data Initialization Started ===");
            log.info("System Module: {}", systemModuleEnabled ? "ENABLED" : "DISABLED");
            log.info("Quartz Module: {}", quartzModuleEnabled ? "ENABLED" : "DISABLED");
            log.info("Generator Module: {}", generatorModuleEnabled ? "ENABLED" : "DISABLED");

            // Initialize schema first (always runs)
            if (!isTableCreated("SYS_CONFIG")) {
                log.info("=== First startup - Initializing base schema ===");
                runScript("schema.sql");
            }

            // Initialize system module data
            if (systemModuleEnabled && !isTableCreated("SYS_USER")) {
                log.info("=== Initializing System Module Data ===");
                runScript("data.sql");
            }

            // Initialize quartz module data
            if (quartzModuleEnabled && !isTableCreated("SYS_JOB")) {
                log.info("=== Initializing Quartz Module Data ===");
                runScript("data-quartz.sql");
            }

            // Initialize generator module data
            if (generatorModuleEnabled && !isTableCreated("GEN_TABLE")) {
                log.info("=== Initializing Generator Module Data ===");
                runScript("data-generator.sql");
            }

            // Initialize pending features (always runs if tables don't exist)
            if (!isTableCreated("SYS_JOB_EMAIL_TEMPLATE")) {
                log.info("=== Initializing Pending Features Data ===");
                runScript("data-pending-features.sql");
            }

            log.info("=== Module Data Initialization Completed ===");
        };
    }

    /**
     * Run SQL script from classpath
     */
    private void runScript(String scriptName) {
        try {
            Resource resource = new ClassPathResource(scriptName);
            if (resource.exists()) {
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(resource);
                populator.setContinueOnError(true);
                populator.setSeparator(";");
                populator.execute(dataSource);
                log.info("Executed script: {}", scriptName);
            } else {
                log.warn("Script not found: {}", scriptName);
            }
        } catch (Exception e) {
            log.error("Failed to execute script: {}", scriptName, e);
        }
    }

    /**
     * Check if a table exists in the database
     */
    private boolean isTableCreated(String tableName) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"});
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (Exception e) {
            log.warn("Error checking if table {} exists: {}", tableName, e.getMessage());
            return false;
        }
    }
}
