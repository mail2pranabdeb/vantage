package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"login_name"}),
    @UniqueConstraint(columnNames = {"email"})
})
@Data
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_name", length = 30, nullable = false)
    private String loginName;

    @Column(name = "user_name", length = 30)
    private String userName;

    @Column(name = "user_type", length = 2)
    private String userType = "00";

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "phonenumber", length = 11)
    private String phonenumber;

    @Column(name = "sex", length = 1)
    private String sex = "0";

    @Column(name = "avatar", length = 100)
    private String avatar;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "salt", length = 20)
    private String salt;

    @Column(name = "status", length = 1)
    private String status = "0";

    @Column(name = "del_flag", length = 1)
    private String delFlag = "0";

    @Column(name = "login_ip", length = 128)
    private String loginIp;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "pwd_update_date")
    private LocalDateTime pwdUpdateDate;

    @Column(name = "create_by", length = 64, updatable = false)
    @CreatedBy
    private String createBy;

    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    @LastModifiedBy
    private String updateBy;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Transient
    private Long[] roleIds;
}