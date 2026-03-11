package com.pd.modules.generator.infrastructure.repository;

import com.pd.modules.generator.domain.GenTableColumn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class GenTableColumnRepository {

    private final JdbcTemplate jdbcTemplate;

    public GenTableColumnRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GenTableColumn> rowMapper = (rs, rowNum) -> {
        GenTableColumn column = new GenTableColumn();
        column.setColumnId(rs.getLong("column_id"));
        column.setTableId(rs.getLong("table_id"));
        column.setColumnName(rs.getString("column_name"));
        column.setColumnComment(rs.getString("column_comment"));
        column.setColumnType(rs.getString("column_type"));
        column.setJavaType(rs.getString("java_type"));
        column.setJavaField(rs.getString("java_field"));
        column.setIsPk(rs.getString("is_pk"));
        column.setIsIncrement(rs.getString("is_increment"));
        column.setIsRequired(rs.getString("is_required"));
        column.setIsInsert(rs.getString("is_insert"));
        column.setIsEdit(rs.getString("is_edit"));
        column.setIsList(rs.getString("is_list"));
        column.setIsQuery(rs.getString("is_query"));
        column.setQueryType(rs.getString("query_type"));
        column.setHtmlType(rs.getString("html_type"));
        column.setDictType(rs.getString("dict_type"));
        column.setSort(rs.getInt("sort"));
        column.setCreateBy(rs.getString("create_by"));
        java.sql.Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            column.setCreateTime(createTime.toLocalDateTime());
        }
        column.setUpdateBy(rs.getString("update_by"));
        java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            column.setUpdateTime(updateTime.toLocalDateTime());
        }
        return column;
    };

    public List<GenTableColumn> findByTableId(Long tableId) {
        return jdbcTemplate.query(
                "SELECT * FROM gen_table_column WHERE table_id = ? ORDER BY sort",
                rowMapper,
                tableId);
    }

    public Optional<GenTableColumn> findById(Long columnId) {
        List<GenTableColumn> columns = jdbcTemplate.query(
                "SELECT * FROM gen_table_column WHERE column_id = ?",
                rowMapper,
                columnId);
        return columns.stream().findFirst();
    }

    public int insert(GenTableColumn column) {
        String sql = "INSERT INTO gen_table_column (table_id, column_name, column_comment, column_type, java_type, java_field, is_pk, is_increment, is_required, is_insert, is_edit, is_list, is_query, query_type, html_type, dict_type, sort, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                column.getTableId(),
                column.getColumnName(),
                column.getColumnComment(),
                column.getColumnType(),
                column.getJavaType(),
                column.getJavaField(),
                column.getIsPk(),
                column.getIsIncrement(),
                column.getIsRequired(),
                column.getIsInsert(),
                column.getIsEdit(),
                column.getIsList(),
                column.getIsQuery(),
                column.getQueryType(),
                column.getHtmlType(),
                column.getDictType(),
                column.getSort(),
                column.getCreateBy(),
                Timestamp.valueOf(column.getCreateTime()));
    }

    public int update(GenTableColumn column) {
        String sql = "UPDATE gen_table_column SET column_name = ?, column_comment = ?, column_type = ?, java_type = ?, java_field = ?, is_pk = ?, is_increment = ?, is_required = ?, is_insert = ?, is_edit = ?, is_list = ?, is_query = ?, query_type = ?, html_type = ?, dict_type = ?, sort = ?, update_by = ?, update_time = ? WHERE column_id = ?";
        return jdbcTemplate.update(sql,
                column.getColumnName(),
                column.getColumnComment(),
                column.getColumnType(),
                column.getJavaType(),
                column.getJavaField(),
                column.getIsPk(),
                column.getIsIncrement(),
                column.getIsRequired(),
                column.getIsInsert(),
                column.getIsEdit(),
                column.getIsList(),
                column.getIsQuery(),
                column.getQueryType(),
                column.getHtmlType(),
                column.getDictType(),
                column.getSort(),
                column.getUpdateBy(),
                column.getUpdateTime() != null ? Timestamp.valueOf(column.getUpdateTime()) : null,
                column.getColumnId());
    }

    public int deleteById(Long columnId) {
        return jdbcTemplate.update("DELETE FROM gen_table_column WHERE column_id = ?", columnId);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM gen_table_column WHERE column_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }

    public int deleteByTableId(Long tableId) {
        return jdbcTemplate.update("DELETE FROM gen_table_column WHERE table_id = ?", tableId);
    }

    public int batchInsert(List<GenTableColumn> columns) {
        String sql = "INSERT INTO gen_table_column (table_id, column_name, column_comment, column_type, java_type, java_field, is_pk, is_increment, is_required, is_insert, is_edit, is_list, is_query, query_type, html_type, dict_type, sort, create_by, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        List<Object[]> batchArgs = new java.util.ArrayList<>();
        for (GenTableColumn column : columns) {
            batchArgs.add(new Object[]{
                    column.getTableId(),
                    column.getColumnName(),
                    column.getColumnComment(),
                    column.getColumnType(),
                    column.getJavaType(),
                    column.getJavaField(),
                    column.getIsPk(),
                    column.getIsIncrement(),
                    column.getIsRequired(),
                    column.getIsInsert(),
                    column.getIsEdit(),
                    column.getIsList(),
                    column.getIsQuery(),
                    column.getQueryType(),
                    column.getHtmlType(),
                    column.getDictType(),
                    column.getSort(),
                    column.getCreateBy(),
                    Timestamp.valueOf(column.getCreateTime())
            });
        }
        
        return jdbcTemplate.batchUpdate(sql, batchArgs).length;
    }
}
