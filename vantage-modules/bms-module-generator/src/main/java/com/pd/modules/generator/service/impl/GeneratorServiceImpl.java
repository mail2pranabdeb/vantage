package com.pd.modules.generator.service.impl;

import com.pd.modules.generator.api.GeneratorService;
import com.pd.modules.generator.domain.GenTable;
import com.pd.modules.generator.domain.GenTableColumn;
import com.pd.modules.generator.infrastructure.repository.GenTableColumnRepository;
import com.pd.modules.generator.infrastructure.repository.GenTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of GeneratorService API.
 * This is the public implementation that external modules should use.
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    private final GenTableRepository tableRepository;
    private final GenTableColumnRepository columnRepository;

    public GeneratorServiceImpl(GenTableRepository tableRepository, GenTableColumnRepository columnRepository) {
        this.tableRepository = tableRepository;
        this.columnRepository = columnRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenTable> findAllTables() {
        return tableRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenTable> findTableById(Long tableId) {
        return tableRepository.findById(tableId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenTableColumn> findColumnsByTableId(Long tableId) {
        return columnRepository.findByTableId(tableId);
    }

    @Override
    @Transactional
    public void importTables(List<String> tableNames) {
        // Implementation would import table schemas from database
        // This is a placeholder for the actual implementation
        for (String tableName : tableNames) {
            GenTable table = new GenTable();
            table.setTableName(tableName);
            table.setCreateTime(java.time.LocalDateTime.now());
            table.setCreateBy("admin");
            tableRepository.insert(table);
        }
    }

    @Override
    @Transactional
    public void updateTable(GenTable table) {
        table.setUpdateTime(java.time.LocalDateTime.now());
        table.setUpdateBy("admin");
        tableRepository.update(table);
    }

    @Override
    @Transactional
    public void deleteTables(Long[] tableIds) {
        for (Long tableId : tableIds) {
            tableRepository.deleteById(tableId);
            columnRepository.deleteByTableId(tableId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> previewCode(Long tableId) {
        // Implementation would generate and return code preview
        // This is a placeholder for the actual implementation
        return Map.of();
    }

    @Override
    @Transactional
    public void generateCode(Long tableId, String genPath) {
        // Implementation would generate code to specified path
        // This is a placeholder for the actual implementation
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadCode(Long tableId) {
        // Implementation would generate ZIP with code
        // This is a placeholder for the actual implementation
        return new byte[0];
    }

    @Override
    @Transactional
    public void syncDatabase(String tableName) {
        // Implementation would sync database schema
        // This is a placeholder for the actual implementation
    }
}
