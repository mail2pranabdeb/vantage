package com.pd.modules.generator.api.dto;

import lombok.Data;

@Data
public class CloneTableRequest {
    private String sourceTableName;
    private String newTableName;
    private String newTableComment;
    private String datasourceKey;
}
