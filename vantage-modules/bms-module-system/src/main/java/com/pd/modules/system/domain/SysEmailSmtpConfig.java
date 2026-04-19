package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_email_smtp_config")
@Data
public class SysEmailSmtpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
}