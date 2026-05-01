package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_dict_type")
@Data
public class SysDictType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dict_id")
    private Long dictId;

    @Column(name = "dict_name", length = 100)
    private String dictName;

    @Column(name = "dict_type", length = 100)
    private String dictType;

    @Column(name = "status", length = 1)
    private String status = "0";

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
}