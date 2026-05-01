package com.pd.modules.generator.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for GenTable entity.
 * Used for data transfer between API and clients.
 */
@Data
public class GenTableDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long tableId;
    private String tableName;
    private String tableComment;
    private String subTableName;
    private String subTableFkName;
    private String className;
    private String tplCategory;
    private String packageName;
    private String moduleName;
    private String businessName;
    private String functionName;
    private String functionAuthor;
    private Integer formColNum;
    private String genType;
    private String genPath;
    private String options;
    private String treeCode;
    private String treeParentCode;
    private String treeName;
    private String parentMenuId;
    private String parentMenuName;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private String remark;
}
