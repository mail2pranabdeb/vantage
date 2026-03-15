package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * System operation log entity - sys_oper_log
 */
@Entity
@Table(name = "sys_oper_log")
@SequenceGenerator(name = "oper_log_seq", sequenceName = "sys_oper_log_seq", allocationSize = 1)
@Data
public class SysOperLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oper_log_seq")
    @Column(name = "oper_id")
    private Long operId;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "business_type")
    private Integer businessType;

    @Column(name = "method", length = 500)
    private String method;

    @Column(name = "request_method", length = 20)
    private String requestMethod;

    @Column(name = "operator_type")
    private Integer operatorType;

    @Column(name = "oper_name", length = 50)
    private String operName;

    @Column(name = "dept_name", length = 50)
    private String deptName;

    @Column(name = "oper_url", length = 500)
    private String operUrl;

    @Column(name = "oper_ip", length = 128)
    private String operIp;

    @Column(name = "oper_location", length = 500)
    private String operLocation;

    @Column(name = "oper_param", length = 4000)
    private String operParam;

    @Column(name = "json_result", length = 8000)
    private String jsonResult;

    @Column(name = "status")
    private Integer status;

    @Column(name = "error_msg", length = 4000)
    private String errorMsg;

    @Column(name = "oper_time")
    @CreationTimestamp
    private LocalDateTime operTime;

    @Column(name = "cost_time")
    private Long costTime;
}
