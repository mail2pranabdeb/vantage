package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MenuDTO {
    private Long menuId;
    private String menuName;
    private Long parentId = 0L;
    private Integer orderNum = 0;
    private String url;
    private String target;
    private String menuType = "M";
    private String visible = "0";
    private String isRefresh = "1";
    private String perms;
    private String icon;
    private String status = "0";
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private java.util.List<MenuDTO> children;
}
