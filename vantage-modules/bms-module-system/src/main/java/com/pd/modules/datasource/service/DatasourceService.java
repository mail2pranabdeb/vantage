package com.pd.modules.datasource.service;

import com.pd.modules.datasource.domain.SysDatasource;
import com.pd.modules.datasource.infrastructure.repository.SysDatasourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DatasourceService {

    private static final Logger log = LoggerFactory.getLogger(DatasourceService.class);

    @Autowired
    private SysDatasourceRepository datasourceRepository;

    public List<SysDatasource> findAll() {
        return datasourceRepository.findAll();
    }

    public Optional<SysDatasource> findById(Long id) {
        return datasourceRepository.findById(id);
    }

    @Transactional
    public SysDatasource save(SysDatasource datasource) {
        if (datasource.getDatasourceId() == null) {
            datasource.setCreateBy("admin");
            datasource.setCreateTime(LocalDateTime.now());
            datasource.setStatus("0");
        } else {
            datasource.setUpdateBy("admin");
            datasource.setUpdateTime(LocalDateTime.now());
        }
        return datasourceRepository.save(datasource);
    }

    @Transactional
    public void deleteById(Long id) {
        datasourceRepository.deleteById(id);
    }

    public boolean existsByDatasourceKey(String datasourceKey) {
        return datasourceRepository.existsByDatasourceKey(datasourceKey);
    }

    /**
     * Test database connection
     */
    public boolean testConnection(SysDatasource datasource) {
        Connection conn = null;
        try {
            Class.forName(datasource.getDriverClass());
            conn = DriverManager.getConnection(
                datasource.getUrl(),
                datasource.getUsername(),
                datasource.getPassword()
            );
            
            boolean isValid = conn.isValid(5);
            
            // Update test status
            datasource.setLastTestTime(LocalDateTime.now());
            datasource.setLastTestStatus(isValid ? "0" : "1");
            datasourceRepository.save(datasource);
            
            return isValid;
        } catch (Exception e) {
            log.error("Connection test failed for datasource: {}", datasource.getDatasourceKey(), e);
            
            // Update test status
            datasource.setLastTestTime(LocalDateTime.now());
            datasource.setLastTestStatus("1");
            datasourceRepository.save(datasource);
            
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) {}
            }
        }
    }

    /**
     * Get driver class by DB type
     */
    public String getDriverClass(String dbType) {
        switch (dbType.toUpperCase()) {
            case "H2":
                return "org.h2.Driver";
            case "MYSQL":
                return "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL":
            case "POSTGRES":
                return "org.postgresql.Driver";
            case "ORACLE":
                return "oracle.jdbc.OracleDriver";
            case "SQLSERVER":
            case "MSSQL":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                return "";
        }
    }

    /**
     * Get default URL pattern by DB type
     */
    public String getDefaultUrlPattern(String dbType) {
        switch (dbType.toUpperCase()) {
            case "H2":
                return "jdbc:h2:file:./data/{dbname}";
            case "MYSQL":
                return "jdbc:mysql://{host}:{port}/{dbname}?useSSL=false&serverTimezone=UTC";
            case "POSTGRESQL":
            case "POSTGRES":
                return "jdbc:postgresql://{host}:{port}/{dbname}";
            case "ORACLE":
                return "jdbc:oracle:thin:@//{host}:{port}/{service}";
            case "SQLSERVER":
            case "MSSQL":
                return "jdbc:sqlserver://{host}:{port};databaseName={dbname}";
            default:
                return "";
        }
    }
}
