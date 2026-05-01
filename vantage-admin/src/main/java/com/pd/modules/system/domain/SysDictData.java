package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_dict_data")
@Data
public class SysDictData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dict_code")
    private Long dictCode;

    @Column(name = "dict_sort")
    private Integer dictSort;

    @Column(name = "dict_label", length = 100)
    private String dictLabel;

    @Column(name = "dict_value", length = 100)
    private String dictValue;

    @Column(name = "dict_type", length = 100)
    private String dictType;

    @Column(name = "css_class", length = 100)
    private String cssClass;

    @Column(name = "list_class", length = 100)
    private String listClass;

    @Column(name = "is_default", length = 1)
    private String isDefault;

    @Column(name = "status", length = 1)
    private String status;

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