package com.pd.modules.system.domain;

import lombok.Data;
import java.io.Serializable;

/**
 * User-Role relationship - use as simple DTO
 */
@Data
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long roleId;
}