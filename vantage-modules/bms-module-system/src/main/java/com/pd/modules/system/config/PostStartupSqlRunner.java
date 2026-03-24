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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * Load SQL scripts on first startup only (when tables don't exist)
 */
@Configuration
public class PostStartupSqlRunner {

    private static final Logger log = LoggerFactory.getLogger(PostStartupSqlRunner.class);

    @Autowired
    private DataSource dataSource;

    @Bean
    @Order(1)
    public CommandLineRunner initializeDatabase() {
        return args -> {
            if (!isTableCreated("SYS_CONFIG")) {
                log.info("=== First startup - Initializing database schema and data ===");
                try {
                    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                    populator.addScript(new ClassPathResource("schema.sql"));
                    populator.addScript(new ClassPathResource("data.sql"));
                    populator.addScript(new ClassPathResource("data-pending-features.sql"));
                    populator.setContinueOnError(true);
                    populator.execute(dataSource);
                    log.info("=== Database initialization completed successfully ===");
                } catch (Exception e) {
                    log.error("Failed to initialize database", e);
                }
            } else {
                log.info("=== Database already exists - Skipping initialization ===");
            }
        };
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
