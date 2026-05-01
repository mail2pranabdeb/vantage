package com.pd.modules.system.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long userId;
    private String loginName;
    private String userName;
    private String userType = "00";
    private String email;
    private String phonenumber;
    private String sex = "0";
    private String avatar;
    private String password;
    private String salt;
    private String status = "0";
    private String delFlag = "0";
    private String loginIp;
    private LocalDateTime loginDate;
    private LocalDateTime pwdUpdateDate;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
    private Long[] roleIds;
}
