package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysConfigRepository {
    private final JdbcTemplate jdbcTemplate;

    public SysConfigRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysConfig> rowMapper = (rs, rowNum) -> {
        SysConfig config = new SysConfig();
        config.setConfigId(rs.getLong("config_id"));
        config.setConfigName(rs.getString("config_name"));
        config.setConfigKey(rs.getString("config_key"));
        config.setConfigValue(rs.getString("config_value"));
        config.setConfigType(rs.getString("config_type"));
        return config;
    };

    public List<SysConfig> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_config", rowMapper);
    }
}
