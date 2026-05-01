package com.pd.modules.system.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;

/**
 * Role-Menu association entity - use as simple DTO, not JPA entity
 */
@Data
public class SysRoleMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long roleId;
    private Long menuId;
}