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
        data.setCssClass(rs.getString("css_class"));
        data.setListClass(rs.getString("list_class"));
        data.setIsDefault(rs.getString("is_default"));
        data.setStatus(rs.getString("status"));
        data.setCreateBy(rs.getString("create_by"));
        data.setCreateTime(rs.getTimestamp("create_time") != null ? rs.getTimestamp("create_time").toLocalDateTime() : null);
        data.setUpdateBy(rs.getString("update_by"));
        data.setUpdateTime(rs.getTimestamp("update_time") != null ? rs.getTimestamp("update_time").toLocalDateTime() : null);
        data.setRemark(rs.getString("remark"));
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

    public Optional<SysDictData> findById(Long dictCode) {
        try {
            SysDictData data = jdbcTemplate.queryForObject(
                    "SELECT * FROM sys_dict_data WHERE dict_code = ?",
                    rowMapper,
                    dictCode
            );
            return Optional.ofNullable(data);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public SysDictData save(SysDictData dictData) {
        if (dictData.getDictCode() == null) {
            // Insert
            jdbcTemplate.update(
                    "INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    dictData.getDictSort(),
                    dictData.getDictLabel(),
                    dictData.getDictValue(),
                    dictData.getDictType(),
                    dictData.getCssClass(),
                    dictData.getListClass(),
                    dictData.getIsDefault(),
                    dictData.getStatus(),
                    dictData.getCreateBy(),
                    dictData.getCreateTime(),
                    dictData.getUpdateBy(),
                    dictData.getUpdateTime(),
                    dictData.getRemark()
            );
            // Get the generated key
            Long generatedKey = jdbcTemplate.queryForObject(
                    "SELECT MAX(dict_code) FROM sys_dict_data",
                    Long.class
            );
            dictData.setDictCode(generatedKey);
        } else {
            // Update
            jdbcTemplate.update(
                    "UPDATE sys_dict_data SET dict_sort = ?, dict_label = ?, dict_value = ?, dict_type = ?, css_class = ?, list_class = ?, is_default = ?, status = ?, update_by = ?, update_time = ?, remark = ? WHERE dict_code = ?",
                    dictData.getDictSort(),
                    dictData.getDictLabel(),
                    dictData.getDictValue(),
                    dictData.getDictType(),
                    dictData.getCssClass(),
                    dictData.getListClass(),
                    dictData.getIsDefault(),
                    dictData.getStatus(),
                    dictData.getUpdateBy(),
                    dictData.getUpdateTime(),
                    dictData.getRemark(),
                    dictData.getDictCode()
            );
        }
        return dictData;
    }

    public void deleteById(Long dictCode) {
        jdbcTemplate.update("DELETE FROM sys_dict_data WHERE dict_code = ?", dictCode);
    }
}
