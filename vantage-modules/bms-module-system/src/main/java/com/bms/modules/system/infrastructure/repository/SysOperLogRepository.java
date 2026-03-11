package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysOperLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class SysOperLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysOperLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysOperLog> rowMapper = (rs, rowNum) -> {
        SysOperLog log = new SysOperLog();
        log.setOperId(rs.getLong("oper_id"));
        log.setTitle(rs.getString("title"));
        log.setBusinessType(rs.getInt("business_type"));
        log.setMethod(rs.getString("method"));
        log.setRequestMethod(rs.getString("request_method"));
        log.setOperatorType(rs.getInt("operator_type"));
        log.setOperName(rs.getString("oper_name"));
        log.setDeptName(rs.getString("dept_name"));
        log.setOperUrl(rs.getString("oper_url"));
        log.setOperIp(rs.getString("oper_ip"));
        log.setOperLocation(rs.getString("oper_location"));
        log.setOperParam(rs.getString("oper_param"));
        log.setJsonResult(rs.getString("json_result"));
        log.setStatus(rs.getInt("status"));
        log.setErrorMsg(rs.getString("error_msg"));
        java.sql.Timestamp operTime = rs.getTimestamp("oper_time");
        if (operTime != null) {
            log.setOperTime(operTime.toLocalDateTime());
        }
        log.setCostTime(rs.getLong("cost_time"));
        return log;
    };

    public List<SysOperLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_oper_log ORDER BY oper_time DESC", rowMapper);
    }

    public List<SysOperLog> findByCondition(String title, String operName, Integer businessType, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_oper_log WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();
        
        if (title != null && !title.isEmpty()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }
        if (operName != null && !operName.isEmpty()) {
            sql.append(" AND oper_name LIKE ?");
            params.add("%" + operName + "%");
        }
        if (businessType != null) {
            sql.append(" AND business_type = ?");
            params.add(businessType);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        sql.append(" ORDER BY oper_time DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public int insert(SysOperLog log) {
        String sql = "INSERT INTO sys_oper_log (title, business_type, method, request_method, operator_type, oper_name, dept_name, oper_url, oper_ip, oper_location, oper_param, json_result, status, error_msg, oper_time, cost_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                log.getTitle(),
                log.getBusinessType(),
                log.getMethod(),
                log.getRequestMethod(),
                log.getOperatorType(),
                log.getOperName(),
                log.getDeptName(),
                log.getOperUrl(),
                log.getOperIp(),
                log.getOperLocation(),
                log.getOperParam(),
                log.getJsonResult(),
                log.getStatus(),
                log.getErrorMsg(),
                log.getOperTime() != null ? Timestamp.valueOf(log.getOperTime()) : null,
                log.getCostTime());
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM sys_oper_log WHERE oper_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }

    public int clean() {
        return jdbcTemplate.update("TRUNCATE TABLE sys_oper_log");
    }
}
