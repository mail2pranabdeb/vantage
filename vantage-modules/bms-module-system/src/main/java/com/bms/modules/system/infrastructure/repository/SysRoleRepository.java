package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SysRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysRole> rowMapper = (rs, rowNum) -> {
        SysRole role = new SysRole();
        role.setRoleId(rs.getLong("role_id"));
        role.setRoleName(rs.getString("role_name"));
        role.setRoleKey(rs.getString("role_key"));
        role.setRoleSort(rs.getInt("role_sort"));
        role.setDataScope(rs.getString("data_scope"));
        role.setStatus(rs.getString("status"));
        role.setDelFlag(rs.getString("del_flag"));
        return role;
    };

    public List<SysRole> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_role WHERE del_flag = '0'", rowMapper);
    }

    public List<SysRole> findRolesByUserId(Long userId) {
        String sql = "SELECT r.* FROM sys_role r " +
                "JOIN sys_user_role ur ON r.role_id = ur.role_id " +
                "WHERE ur.user_id = ? AND r.status = '0' AND r.del_flag = '0'";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }
}
