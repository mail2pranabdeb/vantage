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

    public java.util.Optional<SysConfig> findById(Long configId) {
        List<SysConfig> configs = jdbcTemplate.query(
                "SELECT * FROM sys_config WHERE config_id = ?",
                rowMapper,
                configId);
        return configs.stream().findFirst();
    }

    public int insert(SysConfig config) {
        String sql = "INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, current_timestamp, ?)";
        return jdbcTemplate.update(sql,
                config.getConfigName(),
                config.getConfigKey(),
                config.getConfigValue(),
                config.getConfigType() != null ? config.getConfigType() : "Y",
                "admin",
                config.getRemark());
    }

    public int update(SysConfig config) {
        String sql = "UPDATE sys_config SET config_name = ?, config_key = ?, config_value = ?, config_type = ?, update_by = ?, update_time = current_timestamp, remark = ? WHERE config_id = ?";
        return jdbcTemplate.update(sql,
                config.getConfigName(),
                config.getConfigKey(),
                config.getConfigValue(),
                config.getConfigType(),
                "admin",
                config.getRemark(),
                config.getConfigId());
    }

    public int deleteById(Long configId) {
        return jdbcTemplate.update("DELETE FROM sys_config WHERE config_id = ?", configId);
    }
}
