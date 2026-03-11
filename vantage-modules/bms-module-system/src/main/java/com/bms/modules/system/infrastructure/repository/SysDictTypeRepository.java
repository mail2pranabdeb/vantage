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

    public SysDictType findById(Long dictId) {
        List<SysDictType> types = jdbcTemplate.query(
                "SELECT * FROM sys_dict_type WHERE dict_id = ?",
                rowMapper,
                dictId);
        return types.stream().findFirst().orElse(null);
    }

    public int save(SysDictType dictType) {
        if (dictType.getDictId() == null) {
            return jdbcTemplate.update(
                    "INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, current_timestamp, ?)",
                    dictType.getDictName(),
                    dictType.getDictType(),
                    dictType.getStatus() != null ? dictType.getStatus() : "0",
                    "admin",
                    dictType.getRemark());
        } else {
            return jdbcTemplate.update(
                    "UPDATE sys_dict_type SET dict_name = ?, dict_type = ?, status = ?, update_by = ?, update_time = current_timestamp, remark = ? WHERE dict_id = ?",
                    dictType.getDictName(),
                    dictType.getDictType(),
                    dictType.getStatus(),
                    "admin",
                    dictType.getRemark(),
                    dictType.getDictId());
        }
    }

    public int deleteById(Long dictId) {
        return jdbcTemplate.update("DELETE FROM sys_dict_type WHERE dict_id = ?", dictId);
    }
}
