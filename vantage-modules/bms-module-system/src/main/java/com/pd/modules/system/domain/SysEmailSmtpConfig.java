package com.pd.modules.system.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_email_smtp_config")
public class SysEmailSmtpConfig {

    @Id
    @SequenceGenerator(name = "smtp_config_seq", sequenceName = "sys_email_smtp_config_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "smtp_config_seq")
    @Column(name = "config_id")
    private Long configId;

    @Column(name = "smtp_host", length = 200, nullable = false)
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort = 587;

    @Column(name = "username", length = 200, nullable = false)
    private String username;

    @Column(name = "password", length = 500, nullable = false)
    private String password;

    @Column(name = "auth", length = 1)
    private String auth = "1";

    @Column(name = "starttls_enable", length = 1)
    private String starttlsEnable = "1";

    @Column(name = "starttls_required", length = 1)
    private String starttlsRequired = "1";

    @Column(name = "ssl_enable", length = 1)
    private String sslEnable = "0";

    @Column(name = "timeout")
    private Integer timeout = 5000;

    @Column(name = "status", length = 1)
    private String status = "0";

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }
    public Integer getSmtpPort() { return smtpPort; }
    public void setSmtpPort(Integer smtpPort) { this.smtpPort = smtpPort; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }
    public String getStarttlsEnable() { return starttlsEnable; }
    public void setStarttlsEnable(String starttlsEnable) { this.starttlsEnable = starttlsEnable; }
    public String getStarttlsRequired() { return starttlsRequired; }
    public void setStarttlsRequired(String starttlsRequired) { this.starttlsRequired = starttlsRequired; }
    public String getSslEnable() { return sslEnable; }
    public void setSslEnable(String sslEnable) { this.sslEnable = sslEnable; }
    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
