package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SysUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysUser> rowMapper = (rs, rowNum) -> {
        SysUser user = new SysUser();
        user.setUserId(rs.getLong("user_id"));
        user.setLoginName(rs.getString("login_name"));
        user.setUserName(rs.getString("user_name"));
        user.setUserType(rs.getString("user_type"));
        user.setEmail(rs.getString("email"));
        user.setPhonenumber(rs.getString("phonenumber"));
        user.setSex(rs.getString("sex"));
        user.setAvatar(rs.getString("avatar"));
        user.setPassword(rs.getString("password"));
        user.setSalt(rs.getString("salt"));
        user.setStatus(rs.getString("status"));
        user.setDelFlag(rs.getString("del_flag"));
        user.setLoginIp(rs.getString("login_ip"));
        java.sql.Timestamp loginDate = rs.getTimestamp("login_date");
        if (loginDate != null)
            user.setLoginDate(loginDate.toLocalDateTime());
        java.sql.Timestamp pwdDate = rs.getTimestamp("pwd_update_date");
        if (pwdDate != null)
            user.setPwdUpdateDate(pwdDate.toLocalDateTime());
        user.setCreateBy(rs.getString("create_by"));
        java.sql.Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null)
            user.setCreateTime(createTime.toLocalDateTime());
        user.setUpdateBy(rs.getString("update_by"));
        java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null)
            user.setUpdateTime(updateTime.toLocalDateTime());
        user.setRemark(rs.getString("remark"));
        return user;
    };

    public List<SysUser> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_user WHERE del_flag = '0'", rowMapper);
    }

    public Optional<SysUser> findByLoginName(String loginName) {
        List<SysUser> users = jdbcTemplate.query(
                "SELECT * FROM sys_user WHERE login_name = ? AND del_flag = '0'",
                rowMapper,
                loginName);
        return users.stream().findFirst();
    }

    public Optional<SysUser> findById(Long userId) {
        List<SysUser> users = jdbcTemplate.query(
                "SELECT * FROM sys_user WHERE user_id = ? AND del_flag = '0'",
                rowMapper,
                userId);
        return users.stream().findFirst();
    }

    public int save(SysUser user) {
        if (user.getUserId() == null) {
            return jdbcTemplate.update(
                    "INSERT INTO sys_user (login_name, user_name, user_type, email, phonenumber, sex, avatar, password, salt, status, del_flag, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '0', ?, ?, ?)",
                    user.getLoginName(),
                    user.getUserName(),
                    user.getUserType() != null ? user.getUserType() : "00",
                    user.getEmail(),
                    user.getPhonenumber(),
                    user.getSex() != null ? user.getSex() : "0",
                    user.getAvatar(),
                    user.getPassword(),
                    user.getSalt(),
                    user.getStatus() != null ? user.getStatus() : "0",
                    user.getCreateBy(),
                    user.getCreateTime(),
                    user.getRemark());
        } else {
            return jdbcTemplate.update(
                    "UPDATE sys_user SET login_name = ?, user_name = ?, email = ?, phonenumber = ?, sex = ?, status = ?, update_by = ?, update_time = ?, remark = ? WHERE user_id = ?",
                    user.getLoginName(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getPhonenumber(),
                    user.getSex(),
                    user.getStatus(),
                    user.getUpdateBy(),
                    user.getUpdateTime(),
                    user.getRemark(),
                    user.getUserId());
        }
    }

    public int deleteById(Long userId) {
        return jdbcTemplate.update("UPDATE sys_user SET del_flag = '2' WHERE user_id = ?", userId);
    }
}
