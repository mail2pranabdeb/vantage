package com.pd.modules.system.domain;

import com.pd.common.annotation.Audited;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "sys_menu")
@Data
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "menu_name", length = 50, nullable = false)
    private String menuName;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "order_num")
    private Integer orderNum = 0;

    @Column(name = "url", length = 200)
    private String url;

    @Column(name = "target", length = 20)
    private String target;

    @Column(name = "menu_type", length = 1)
    private String menuType = "M";

    @Column(name = "visible", length = 1)
    private String visible = "0";

    @Column(name = "is_refresh", length = 1)
    private String isRefresh = "1";

    @Column(name = "perms", length = 500)
    private String perms;

    @Column(name = "icon", length = 100)
    private String icon;

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

    @Transient
    private List<SysMenu> children = new ArrayList<>();
}