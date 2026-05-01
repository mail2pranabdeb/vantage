package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoleDTO {
    private Long roleId;
    private String roleName;
    private String roleKey;
    private Integer roleSort = 0;
    private String dataScope = "1";
    private String status = "0";
    private String delFlag = "0";
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private Long[] menuIds;
    private Long[] deptIds;
}
