package com.pd.modules.generator.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Code generation table entity - gen_table
 */
public class GenTable {

    /** Table ID */
    private Long tableId;

    /** Table name */
    private String tableName;

    /** Table comment */
    private String tableComment;

    /** Sub table name */
    private String subTableName;

    /** Sub table FK name */
    private String subTableFkName;

    /** Class name */
    private String className;

    /** Template category (crud/tree/sub) */
    private String tplCategory;

    /** Package name */
    private String packageName;

    /** Module name */
    private String moduleName;

    /** Business name */
    private String businessName;

    /** Function name */
    private String functionName;

    /** Function author */
    private String functionAuthor;

    /** Form column number */
    private int formColNum;

    /** Generation type (0=zip, 1=custom path) */
    private String genType;

    /** Generation path */
    private String genPath;

    /** Primary key column */
    private GenTableColumn pkColumn;

    /** Sub table */
    private GenTable subTable;

    /** Columns */
    private List<GenTableColumn> columns;

    /** Options */
    private String options;

    /** Tree code field */
    private String treeCode;

    /** Tree parent code field */
    private String treeParentCode;

    /** Tree name field */
    private String treeName;

    /** Parent menu ID */
    private String parentMenuId;

    /** Parent menu name */
    private String parentMenuName;

    /** Create by */
    private String createBy;

    /** Create time */
    private LocalDateTime createTime;

    /** Update by */
    private String updateBy;

    /** Update time */
    private LocalDateTime updateTime;

    /** Remark */
    private String remark;

    // Getters and Setters

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getSubTableName() {
        return subTableName;
    }

    public void setSubTableName(String subTableName) {
        this.subTableName = subTableName;
    }

    public String getSubTableFkName() {
        return subTableFkName;
    }

    public void setSubTableFkName(String subTableFkName) {
        this.subTableFkName = subTableFkName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTplCategory() {
        return tplCategory;
    }

    public void setTplCategory(String tplCategory) {
        this.tplCategory = tplCategory;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionAuthor() {
        return functionAuthor;
    }

    public void setFunctionAuthor(String functionAuthor) {
        this.functionAuthor = functionAuthor;
    }

    public int getFormColNum() {
        return formColNum;
    }

    public void setFormColNum(int formColNum) {
        this.formColNum = formColNum;
    }

    public String getGenType() {
        return genType;
    }

    public void setGenType(String genType) {
        this.genType = genType;
    }

    public String getGenPath() {
        return genPath;
    }

    public void setGenPath(String genPath) {
        this.genPath = genPath;
    }

    public GenTableColumn getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(GenTableColumn pkColumn) {
        this.pkColumn = pkColumn;
    }

    public GenTable getSubTable() {
        return subTable;
    }

    public void setSubTable(GenTable subTable) {
        this.subTable = subTable;
    }

    public List<GenTableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<GenTableColumn> columns) {
        this.columns = columns;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getTreeCode() {
        return treeCode;
    }

    public void setTreeCode(String treeCode) {
        this.treeCode = treeCode;
    }

    public String getTreeParentCode() {
        return treeParentCode;
    }

    public void setTreeParentCode(String treeParentCode) {
        this.treeParentCode = treeParentCode;
    }

    public String getTreeName() {
        return treeName;
    }

    public void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    public String getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getParentMenuName() {
        return parentMenuName;
    }

    public void setParentMenuName(String parentMenuName) {
        this.parentMenuName = parentMenuName;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isSub() {
        return "sub".equals(tplCategory);
    }

    public boolean isTree() {
        return "tree".equals(tplCategory);
    }

    public boolean isCrud() {
        return "crud".equals(tplCategory);
    }
}
