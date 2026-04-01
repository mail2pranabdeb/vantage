package com.pd.modules.datasource.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Multi-Datasource Configuration Entity
 */
@Entity
@Table(name = "sys_datasource")
public class SysDatasource {

    @Id
    @SequenceGenerator(name = "sys_datasource_seq", sequenceName = "sys_datasource_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sys_datasource_seq")
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

    // Getters and Setters
    public Long getDatasourceId() { return datasourceId; }
    public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
    public String getDatasourceName() { return datasourceName; }
    public void setDatasourceName(String datasourceName) { this.datasourceName = datasourceName; }
    public String getDatasourceKey() { return datasourceKey; }
    public void setDatasourceKey(String datasourceKey) { this.datasourceKey = datasourceKey; }
    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getLastTestTime() { return lastTestTime; }
    public void setLastTestTime(LocalDateTime lastTestTime) { this.lastTestTime = lastTestTime; }
    public String getLastTestStatus() { return lastTestStatus; }
    public void setLastTestStatus(String lastTestStatus) { this.lastTestStatus = lastTestStatus; }
}
