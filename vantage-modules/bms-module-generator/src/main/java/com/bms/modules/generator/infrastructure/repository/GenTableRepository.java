package com.pd.modules.generator.infrastructure.repository;

import com.pd.modules.generator.domain.GenTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class GenTableRepository {

    private final JdbcTemplate jdbcTemplate;

    public GenTableRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GenTable> rowMapper = (rs, rowNum) -> {
        GenTable table = new GenTable();
        table.setTableId(rs.getLong("table_id"));
        table.setTableName(rs.getString("table_name"));
        table.setTableComment(rs.getString("table_comment"));
        table.setSubTableName(rs.getString("sub_table_name"));
        table.setSubTableFkName(rs.getString("sub_table_fk_name"));
        table.setClassName(rs.getString("class_name"));
        table.setTplCategory(rs.getString("tpl_category"));
        table.setPackageName(rs.getString("package_name"));
        table.setModuleName(rs.getString("module_name"));
        table.setBusinessName(rs.getString("business_name"));
        table.setFunctionName(rs.getString("function_name"));
        table.setFunctionAuthor(rs.getString("function_author"));
        table.setFormColNum(rs.getInt("form_col_num"));
        table.setGenType(rs.getString("gen_type"));
        table.setGenPath(rs.getString("gen_path"));
        table.setOptions(rs.getString("options"));
        table.setTreeCode(rs.getString("tree_code"));
        table.setTreeParentCode(rs.getString("tree_parent_code"));
        table.setTreeName(rs.getString("tree_name"));
        table.setParentMenuId(rs.getString("parent_menu_id"));
        table.setParentMenuName(rs.getString("parent_menu_name"));
        table.setCreateBy(rs.getString("create_by"));
        java.sql.Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            table.setCreateTime(createTime.toLocalDateTime());
        }
        table.setUpdateBy(rs.getString("update_by"));
        java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            table.setUpdateTime(updateTime.toLocalDateTime());
        }
        table.setRemark(rs.getString("remark"));
        return table;
    };

    public List<GenTable> findAll() {
        return jdbcTemplate.query("SELECT * FROM gen_table", rowMapper);
    }

    public List<GenTable> findByCondition(GenTable condition) {
        StringBuilder sql = new StringBuilder("SELECT * FROM gen_table WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();
        
        if (condition.getTableName() != null && !condition.getTableName().isEmpty()) {
            sql.append(" AND table_name LIKE ?");
            params.add("%" + condition.getTableName() + "%");
        }
        if (condition.getTableComment() != null && !condition.getTableComment().isEmpty()) {
            sql.append(" AND table_comment LIKE ?");
            params.add("%" + condition.getTableComment() + "%");
        }
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<GenTable> findById(Long tableId) {
        List<GenTable> tables = jdbcTemplate.query(
                "SELECT * FROM gen_table WHERE table_id = ?",
                rowMapper,
                tableId);
        return tables.stream().findFirst();
    }

    public Optional<GenTable> findByName(String tableName) {
        List<GenTable> tables = jdbcTemplate.query(
                "SELECT * FROM gen_table WHERE table_name = ?",
                rowMapper,
                tableName);
        return tables.stream().findFirst();
    }

    public int insert(GenTable table) {
        String sql = "INSERT INTO gen_table (table_name, table_comment, sub_table_name, sub_table_fk_name, class_name, tpl_category, package_name, module_name, business_name, function_name, function_author, form_col_num, gen_type, gen_path, options, tree_code, tree_parent_code, tree_name, parent_menu_id, parent_menu_name, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                table.getTableName(),
                table.getTableComment(),
                table.getSubTableName(),
                table.getSubTableFkName(),
                table.getClassName(),
                table.getTplCategory(),
                table.getPackageName(),
                table.getModuleName(),
                table.getBusinessName(),
                table.getFunctionName(),
                table.getFunctionAuthor(),
                table.getFormColNum(),
                table.getGenType(),
                table.getGenPath(),
                table.getOptions(),
                table.getTreeCode(),
                table.getTreeParentCode(),
                table.getTreeName(),
                table.getParentMenuId(),
                table.getParentMenuName(),
                table.getCreateBy(),
                Timestamp.valueOf(table.getCreateTime()),
                table.getRemark());
    }

    public int update(GenTable table) {
        String sql = "UPDATE gen_table SET table_name = ?, table_comment = ?, sub_table_name = ?, sub_table_fk_name = ?, class_name = ?, tpl_category = ?, package_name = ?, module_name = ?, business_name = ?, function_name = ?, function_author = ?, form_col_num = ?, gen_type = ?, gen_path = ?, options = ?, tree_code = ?, tree_parent_code = ?, tree_name = ?, parent_menu_id = ?, parent_menu_name = ?, update_by = ?, update_time = ?, remark = ? WHERE table_id = ?";
        return jdbcTemplate.update(sql,
                table.getTableName(),
                table.getTableComment(),
                table.getSubTableName(),
                table.getSubTableFkName(),
                table.getClassName(),
                table.getTplCategory(),
                table.getPackageName(),
                table.getModuleName(),
                table.getBusinessName(),
                table.getFunctionName(),
                table.getFunctionAuthor(),
                table.getFormColNum(),
                table.getGenType(),
                table.getGenPath(),
                table.getOptions(),
                table.getTreeCode(),
                table.getTreeParentCode(),
                table.getTreeName(),
                table.getParentMenuId(),
                table.getParentMenuName(),
                table.getUpdateBy(),
                table.getUpdateTime() != null ? Timestamp.valueOf(table.getUpdateTime()) : null,
                table.getRemark(),
                table.getTableId());
    }

    public int deleteById(Long tableId) {
        return jdbcTemplate.update("DELETE FROM gen_table WHERE table_id = ?", tableId);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM gen_table WHERE table_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }
}
