package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysDictData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class SysDictDataRepository {
    private final JdbcTemplate jdbcTemplate;

    public SysDictDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysDictData> rowMapper = this::mapRow;

    private SysDictData mapRow(ResultSet rs, int rowNum) throws SQLException {
        SysDictData data = new SysDictData();
        data.setDictCode(rs.getLong("dict_code"));
        data.setDictSort(rs.getInt("dict_sort"));
        data.setDictLabel(rs.getString("dict_label"));
        data.setDictValue(rs.getString("dict_value"));
        data.setDictType(rs.getString("dict_type"));
        data.setIsDefault(rs.getString("is_default"));
        data.setStatus(rs.getString("status"));
        return data;
    }

    public List<SysDictData> findByType(String dictType) {
        return jdbcTemplate.query(
                "SELECT * FROM sys_dict_data WHERE dict_type = ? ORDER BY dict_sort ASC",
                rowMapper,
                dictType
        );
    }

    public List<SysDictData> findByDictTypeOrderBySort(String dictType) {
        return jdbcTemplate.query(
                "SELECT * FROM sys_dict_data WHERE dict_type = ? AND status = '0' ORDER BY dict_sort ASC",
                rowMapper,
                dictType
        );
    }

    public Optional<SysDictData> findByDictTypeAndValue(String dictType, String dictValue) {
        try {
            SysDictData data = jdbcTemplate.queryForObject(
                    "SELECT * FROM sys_dict_data WHERE dict_type = ? AND dict_value = ?",
                    rowMapper,
                    dictType,
                    dictValue
            );
            return Optional.ofNullable(data);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
