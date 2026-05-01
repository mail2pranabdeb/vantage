package com.pd.modules.generator.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "gen_table")
@Data
public class GenTable implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "table_name", length = 200)
    private String tableName;

    @Column(name = "table_comment", length = 500)
    private String tableComment;

    @Column(name = "sub_table_name", length = 64)
    private String subTableName;

    @Column(name = "sub_table_fk_name", length = 64)
    private String subTableFkName;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "tpl_category", length = 200)
    private String tplCategory;

    @Column(name = "package_name", length = 100)
    private String packageName;

    @Column(name = "module_name", length = 30)
    private String moduleName;

    @Column(name = "business_name", length = 30)
    private String businessName;

    @Column(name = "function_name", length = 50)
    private String functionName;

    @Column(name = "function_author", length = 50)
    private String functionAuthor;

    @Column(name = "form_col_num")
    private Integer formColNum;

    @Column(name = "gen_type", length = 1)
    private String genType;

    @Column(name = "gen_path", length = 200)
    private String genPath;

    @Column(name = "options", length = 1000)
    private String options;

    @Column(name = "tree_code", length = 200)
    private String treeCode;

    @Column(name = "tree_parent_code", length = 200)
    private String treeParentCode;

    @Column(name = "tree_name", length = 200)
    private String treeName;

    @Column(name = "parent_menu_id", length = 64)
    private String parentMenuId;

    @Column(name = "parent_menu_name", length = 64)
    private String parentMenuName;

    @Column(name = "create_by", length = 64)
    private String createBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_by", length = 64)
    private String updateBy;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 500)
    private String remark;
}