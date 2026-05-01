package com.pd.modules.generator.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for GenTableColumn entity.
 * Used for data transfer between API and clients.
 */
@Data
public class GenTableColumnDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long columnId;
    private Long tableId;
    private String columnName;
    private String columnComment;
    private String columnType;
    private String javaType;
    private String javaField;
    private String isPk;
    private String isIncrement;
    private String isRequired;
    private String isInsert;
    private String isEdit;
    private String isList;
    private String isQuery;
    private String queryType;
    private String htmlType;
    private String dictType;
    private Integer sort;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    public boolean isSuperColumn() {
        return "true".equals(isPk) || "true".equals(isIncrement);
    }
}
