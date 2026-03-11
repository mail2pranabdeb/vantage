package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysDictType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysDictTypeRepository {
    private final JdbcTemplate jdbcTemplate;

    public SysDictTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysDictType> rowMapper = (rs, rowNum) -> {
        SysDictType type = new SysDictType();
        type.setDictId(rs.getLong("dict_id"));
        type.setDictName(rs.getString("dict_name"));
        type.setDictType(rs.getString("dict_type"));
        type.setStatus(rs.getString("status"));
        return type;
    };

    public List<SysDictType> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_dict_type", rowMapper);
    }
}
