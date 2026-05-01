package com.pd.modules.quartz.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Holiday calendar entity for job scheduling
 */
@Entity
@Table(name = "sys_holiday")
@Data
public class SysHoliday implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long holidayId;

    @Column(name = "holiday_name", length = 100, nullable = false)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_type", length = 1)
    private String holidayType = "1"; // 1=National, 2=Company, 3=Optional

    @Column(name = "is_recurring")
    private Boolean recurring = false;

    @Column(name = "description", length = 500)
    private String description;

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
}
