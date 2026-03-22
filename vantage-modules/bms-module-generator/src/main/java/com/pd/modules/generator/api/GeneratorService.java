package com.pd.modules.generator.api;

import com.pd.modules.generator.domain.GenTable;
import com.pd.modules.generator.domain.GenTableColumn;
import java.util.List;
import java.util.Optional;

/**
 * Generator module public API for code generation operations.
 * This interface defines the contract for external modules to interact with the generator module.
 */
public interface GeneratorService {

    /**
     * Get all tables available for generation
     * @return list of tables
     */
    List<GenTable> findAllTables();

    /**
     * Get table by ID
     * @param tableId the table ID
     * @return optional containing the table if found
     */
    Optional<GenTable> findTableById(Long tableId);

    /**
     * Get table columns by table ID
     * @param tableId the table ID
     * @return list of columns
     */
    List<GenTableColumn> findColumnsByTableId(Long tableId);

    /**
     * Import table schema from database
     * @param tableNames list of table names to import
     */
    void importTables(List<String> tableNames);

    /**
     * Update generator table configuration
     * @param table the table configuration to update
     */
    void updateTable(GenTable table);

    /**
     * Delete generator table configuration
     * @param tableIds list of table IDs to delete
     */
    void deleteTables(Long[] tableIds);

    /**
     * Preview generated code
     * @param tableId the table ID
     * @return map of file names to content
     */
    java.util.Map<String, String> previewCode(Long tableId);

    /**
     * Generate code to specified path
     * @param tableId the table ID
     * @param genPath the generation path
     */
    void generateCode(Long tableId, String genPath);

    /**
     * Download generated code as ZIP
     * @param tableId the table ID
     * @return ZIP file bytes
     */
    byte[] downloadCode(Long tableId);

    /**
     * Sync database schema with generator table
     * @param tableName the table name to sync
     */
    void syncDatabase(String tableName);
}
