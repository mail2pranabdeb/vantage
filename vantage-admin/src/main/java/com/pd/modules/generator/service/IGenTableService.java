package com.pd.modules.generator.service;

import java.util.List;
import java.util.Map;
import com.pd.modules.generator.domain.GenTable;
import com.pd.modules.generator.domain.GenTableColumn;

/**
 * Code generation service interface
 */
public interface IGenTableService {

    /**
     * Get list of tables
     */
    List<GenTable> selectGenTableList(GenTable genTable);

    /**
     * Get list of database tables
     */
    List<GenTable> selectDbTableList(GenTable genTable);

    /**
     * Get table by ID
     */
    GenTable selectGenTableById(Long tableId);

    /**
     * Get table columns by table ID
     */
    List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * Import table schema
     */
    void importGenTable(String tables);

    /**
     * Update generator table
     */
    int updateGenTable(GenTable genTable);

    /**
     * Delete generator tables by IDs
     */
    int deleteGenTableByIds(Long[] tableIds);

    /**
     * Export table data
     */
    byte[] exportTableData(String tableName);

    /**
     * Download code as zip
     */
    byte[] downloadZipData(String tableName);

    /**
     * Get project source path
     */
    String getProjectSourcePath();

    /**
     * Get package path
     */
    String getPackagePath(String tableName);

    /**
     * Get template path
     */
    Map<String, String> getTemplatePath(String tableName);
}
