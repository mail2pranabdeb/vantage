package com.pd.modules.system.datasource.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Multi-Datasource Configuration Entity
 */
@Entity
@Table(name = "sys_datasource")
@Data
public class SysDatasource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "datasource_id")
    private Long datasourceId;

    @Column(name = "datasource_name", length = 100, nullable = false)
    private String datasourceName;

    @Column(name = "datasource_key", length = 50, nullable = false, unique = true)
    private String datasourceKey;

    @Column(name = "db_type", length = 20, nullable = false)
    private String dbType; // H2, MySQL, PostgreSQL, Oracle, SQLServer

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "password", length = 500, nullable = false)
    private String password;

    @Column(name = "driver_class", length = 200, nullable = false)
    private String driverClass;

    @Column(name = "status", length = 1)
    private String status = "0"; // 0=Active, 1=Inactive

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

    @Column(name = "last_test_time")
    private LocalDateTime lastTestTime;

    @Column(name = "last_test_status", length = 1)
    private String lastTestStatus = "0"; // 0=Success, 1=Failed

    }
