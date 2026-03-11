package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysLogininfor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class SysLogininforRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysLogininforRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysLogininfor> rowMapper = (rs, rowNum) -> {
        SysLogininfor info = new SysLogininfor();
        info.setInfoId(rs.getLong("info_id"));
        info.setLoginName(rs.getString("login_name"));
        info.setStatus(rs.getString("status"));
        info.setIpaddr(rs.getString("ipaddr"));
        info.setLoginLocation(rs.getString("login_location"));
        info.setBrowser(rs.getString("browser"));
        info.setOs(rs.getString("os"));
        info.setMsg(rs.getString("msg"));
        java.sql.Timestamp loginTime = rs.getTimestamp("login_time");
        if (loginTime != null) {
            info.setLoginTime(loginTime.toLocalDateTime());
        }
        return info;
    };

    public List<SysLogininfor> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_logininfor ORDER BY login_time DESC", rowMapper);
    }

    public List<SysLogininfor> findByCondition(String loginName, String status, String ipaddr) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_logininfor WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();
        
        if (loginName != null && !loginName.isEmpty()) {
            sql.append(" AND login_name LIKE ?");
            params.add("%" + loginName + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (ipaddr != null && !ipaddr.isEmpty()) {
            sql.append(" AND ipaddr LIKE ?");
            params.add("%" + ipaddr + "%");
        }
        
        sql.append(" ORDER BY login_time DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public int insert(SysLogininfor info) {
        String sql = "INSERT INTO sys_logininfor (login_name, status, ipaddr, login_location, browser, os, msg, login_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                info.getLoginName(),
                info.getStatus(),
                info.getIpaddr(),
                info.getLoginLocation(),
                info.getBrowser(),
                info.getOs(),
                info.getMsg(),
                info.getLoginTime() != null ? Timestamp.valueOf(info.getLoginTime()) : null);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM sys_logininfor WHERE info_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }

    public int clean() {
        return jdbcTemplate.update("TRUNCATE TABLE sys_logininfor");
    }
}
