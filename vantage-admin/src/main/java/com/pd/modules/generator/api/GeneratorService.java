package com.pd.modules.generator.api;

import com.pd.modules.generator.api.dto.GenTableDto;
import com.pd.modules.generator.api.dto.GenTableColumnDto;
import java.util.List;
import java.util.Optional;

/**
 * Generator module public API for code generation operations.
 * This interface defines the contract for external modules to interact with the generator module.
 * Uses DTOs instead of domain entities for data transfer.
 */
public interface GeneratorService {

    /**
     * Get all tables available for generation
     * @return list of table DTOs
     */
    List<GenTableDto> findAllTables();

    /**
     * Get table by ID
     * @param tableId the table ID
     * @return optional containing the table DTO if found
     */
    Optional<GenTableDto> findTableById(Long tableId);

    /**
     * Get table columns by table ID
     * @param tableId the table ID
     * @return list of column DTOs
     */
    List<GenTableColumnDto> findColumnsByTableId(Long tableId);

    /**
     * Import table schema from database
     * @param tableNames list of table names to import
     */
    void importTables(List<String> tableNames);

    /**
     * Update generator table configuration
     * @param tableDto the table DTO to update
     */
    void updateTable(GenTableDto tableDto);

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
