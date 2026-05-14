package com.pd.modules.generator.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateTableRequest {
    private String tableName;
    private String tableComment;
    private String datasourceKey;
    private List<ColumnDefinition> columns;

    @Data
    public static class ColumnDefinition {
        private String columnName;
        private String columnType;
        private Integer columnLength = 255;
        private boolean nullable = true;
        private boolean isPrimaryKey;
        private String defaultValue;
        private String columnComment;
    }
}
