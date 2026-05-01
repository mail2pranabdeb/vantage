package com.pd.modules.generator.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "gen_table_column")
@Data
public class GenTableColumn implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_id")
    private Long columnId;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "column_name", length = 200)
    private String columnName;

    @Column(name = "column_comment", length = 500)
    private String columnComment;

    @Column(name = "column_type", length = 100)
    private String columnType;

    @Column(name = "java_type", length = 500)
    private String javaType;

    @Column(name = "java_field", length = 200)
    private String javaField;

    @Column(name = "is_pk", length = 1)
    private String isPk;

    @Column(name = "is_increment", length = 1)
    private String isIncrement;

    @Column(name = "is_required", length = 1)
    private String isRequired;

    @Column(name = "is_insert", length = 1)
    private String isInsert;

    @Column(name = "is_edit", length = 1)
    private String isEdit;

    @Column(name = "is_list", length = 1)
    private String isList;

    @Column(name = "is_query", length = 1)
    private String isQuery;

    @Column(name = "query_type", length = 200)
    private String queryType;

    @Column(name = "html_type", length = 200)
    private String htmlType;

    @Column(name = "dict_type", length = 200)
    private String dictType;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "create_by", length = 64)
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public boolean isSuperColumn() {
        return "true".equals(isPk) || "true".equals(isIncrement);
    }
}