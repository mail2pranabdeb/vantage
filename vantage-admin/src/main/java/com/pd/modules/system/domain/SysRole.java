package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_role")
@Data
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", length = 30, nullable = false)
    private String roleName;

    @Column(name = "role_key", length = 100, nullable = false)
    private String roleKey;

    @Column(name = "role_sort", nullable = false)
    private Integer roleSort = 0;

    @Column(name = "data_scope", length = 1)
    private String dataScope = "1";

    @Column(name = "status", length = 1)
    private String status = "0";

    @Column(name = "del_flag", length = 1)
    private String delFlag = "0";

    @Column(name = "create_by", length = 64)
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @Transient
    private Long[] menuIds;

    @Transient
    private Long[] deptIds;
}