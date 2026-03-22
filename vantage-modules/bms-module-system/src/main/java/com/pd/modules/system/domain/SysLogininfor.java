package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * System login info entity - sys_logininfor
 */
@Entity
@Table(name = "sys_logininfor")
@SequenceGenerator(name = "logininfor_seq", sequenceName = "sys_logininfor_seq", allocationSize = 1)
@Data
public class SysLogininfor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logininfor_seq")
    @Column(name = "info_id")
    private Long infoId;

    @Column(name = "login_name", length = 50)
    private String loginName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "ipaddr", length = 128)
    private String ipaddr;

    @Column(name = "login_location", length = 255)
    private String loginLocation;

    @Column(name = "browser", length = 50)
    private String browser;

    @Column(name = "os", length = 50)
    private String os;

    @Column(name = "msg", length = 255)
    private String msg;

    @Column(name = "login_time")
    @CreationTimestamp
    private LocalDateTime loginTime;
}
