package com.pd.modules.system.datasource.service.impl;

import com.pd.modules.system.api.SystemDatasourceService;
import com.pd.modules.system.api.dto.DatasourceDTO;
import com.pd.modules.system.datasource.domain.SysDatasource;
import com.pd.modules.system.datasource.infrastructure.repository.SysDatasourceRepository;
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
import java.util.stream.Collectors;

@Service
public class DatasourceServiceImpl implements SystemDatasourceService {

    private static final Logger log = LoggerFactory.getLogger(DatasourceServiceImpl.class);

    @Autowired
    private SysDatasourceRepository datasourceRepository;

    @Override
    public List<DatasourceDTO> findAll() {
        return datasourceRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DatasourceDTO> findById(Long id) {
        return datasourceRepository.findById(id).map(this::toDTO);
    }

    @Override
    @Transactional
    public DatasourceDTO save(DatasourceDTO dto) {
        SysDatasource entity = toEntity(dto);
        if (entity.getDatasourceId() == null) {
            entity.setCreateBy("admin");
            entity.setCreateTime(LocalDateTime.now());
            entity.setStatus("0");
        } else {
            entity.setUpdateBy("admin");
            entity.setUpdateTime(LocalDateTime.now());
        }
        SysDatasource saved = datasourceRepository.save(entity);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        datasourceRepository.deleteById(id);
    }

    @Override
    public boolean existsByDatasourceKey(String datasourceKey) {
        return datasourceRepository.existsByDatasourceKey(datasourceKey);
    }

    @Override
    public boolean testConnection(DatasourceDTO dto) {
        SysDatasource datasource = toEntity(dto);
        Connection conn = null;
        try {
            Class.forName(datasource.getDriverClass());
            conn = DriverManager.getConnection(
                datasource.getUrl(),
                datasource.getUsername(),
                datasource.getPassword()
            );
            
            boolean isValid = conn.isValid(5);
            
            datasource.setLastTestTime(LocalDateTime.now());
            datasource.setLastTestStatus(isValid ? "0" : "1");
            datasourceRepository.save(datasource);
            
            return isValid;
        } catch (Exception e) {
            log.error("Connection test failed for datasource: {}", datasource.getDatasourceKey(), e);
            
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

    @Override
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

    @Override
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

    private DatasourceDTO toDTO(SysDatasource entity) {
        if (entity == null) return null;
        DatasourceDTO dto = new DatasourceDTO();
        dto.setDatasourceId(entity.getDatasourceId());
        dto.setDatasourceName(entity.getDatasourceName());
        dto.setDatasourceKey(entity.getDatasourceKey());
        dto.setDbType(entity.getDbType());
        dto.setUrl(entity.getUrl());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setDriverClass(entity.getDriverClass());
        dto.setStatus(entity.getStatus());
        dto.setCreateBy(entity.getCreateBy());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateBy(entity.getUpdateBy());
        dto.setUpdateTime(entity.getUpdateTime());
        dto.setRemark(entity.getRemark());
        dto.setLastTestTime(entity.getLastTestTime());
        dto.setLastTestStatus(entity.getLastTestStatus());
        return dto;
    }

    private SysDatasource toEntity(DatasourceDTO dto) {
        if (dto == null) return null;
        SysDatasource entity = new SysDatasource();
        entity.setDatasourceId(dto.getDatasourceId());
        entity.setDatasourceName(dto.getDatasourceName());
        entity.setDatasourceKey(dto.getDatasourceKey());
        entity.setDbType(dto.getDbType());
        entity.setUrl(dto.getUrl());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        entity.setDriverClass(dto.getDriverClass());
        entity.setStatus(dto.getStatus());
        entity.setCreateBy(dto.getCreateBy());
        entity.setCreateTime(dto.getCreateTime());
        entity.setUpdateBy(dto.getUpdateBy());
        entity.setUpdateTime(dto.getUpdateTime());
        entity.setRemark(dto.getRemark());
        entity.setLastTestTime(dto.getLastTestTime());
        entity.setLastTestStatus(dto.getLastTestStatus());
        return entity;
    }
}
