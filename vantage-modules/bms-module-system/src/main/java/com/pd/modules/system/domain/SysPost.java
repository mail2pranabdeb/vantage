package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_post")
@Data
public class SysPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "post_code", length = 64, nullable = false)
    private String postCode;

    @Column(name = "post_name", length = 50, nullable = false)
    private String postName;

    @Column(name = "post_sort", nullable = false)
    private Integer postSort = 0;

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