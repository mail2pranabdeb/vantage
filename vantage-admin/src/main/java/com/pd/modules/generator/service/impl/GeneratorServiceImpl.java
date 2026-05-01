package com.pd.modules.generator.service.impl;

import com.pd.modules.generator.api.GeneratorService;
import com.pd.modules.generator.api.dto.GenTableDto;
import com.pd.modules.generator.api.dto.GenTableColumnDto;
import com.pd.modules.generator.domain.GenTable;
import com.pd.modules.generator.domain.GenTableColumn;
import com.pd.modules.generator.infrastructure.repository.GenTableColumnRepository;
import com.pd.modules.generator.infrastructure.repository.GenTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of GeneratorService API.
 * This is the public implementation that external modules should use.
 * Converts between domain entities and DTOs.
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
    public List<GenTableDto> findAllTables() {
        return tableRepository.findAll().stream()
                .map(this::convertToTableDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GenTableDto> findTableById(Long tableId) {
        return tableRepository.findById(tableId)
                .map(this::convertToTableDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenTableColumnDto> findColumnsByTableId(Long tableId) {
        return columnRepository.findByTableId(tableId).stream()
                .map(this::convertToColumnDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void importTables(List<String> tableNames) {
        for (String tableName : tableNames) {
            GenTable table = new GenTable();
            table.setTableName(tableName);
            table.setCreateTime(LocalDateTime.now());
            table.setCreateBy("admin");
            tableRepository.save(table);
        }
    }

    @Override
    @Transactional
    public void updateTable(GenTableDto tableDto) {
        GenTable table = convertToTableEntity(tableDto);
        table.setUpdateTime(LocalDateTime.now());
        table.setUpdateBy("admin");
        tableRepository.save(table);
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
        return Map.of(
            "Entity.java", "// Entity class for table " + tableId,
            "Repository.java", "// Repository interface",
            "Service.java", "// Service implementation",
            "Controller.java", "// REST Controller"
        );
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

    /**
     * Convert GenTable entity to GenTableDto
     */
    private GenTableDto convertToTableDto(GenTable table) {
        if (table == null) return null;
        GenTableDto dto = new GenTableDto();
        dto.setTableId(table.getTableId());
        dto.setTableName(table.getTableName());
        dto.setTableComment(table.getTableComment());
        dto.setSubTableName(table.getSubTableName());
        dto.setSubTableFkName(table.getSubTableFkName());
        dto.setClassName(table.getClassName());
        dto.setTplCategory(table.getTplCategory());
        dto.setPackageName(table.getPackageName());
        dto.setModuleName(table.getModuleName());
        dto.setBusinessName(table.getBusinessName());
        dto.setFunctionName(table.getFunctionName());
        dto.setFunctionAuthor(table.getFunctionAuthor());
        dto.setFormColNum(table.getFormColNum());
        dto.setGenType(table.getGenType());
        dto.setGenPath(table.getGenPath());
        dto.setOptions(table.getOptions());
        dto.setTreeCode(table.getTreeCode());
        dto.setTreeParentCode(table.getTreeParentCode());
        dto.setTreeName(table.getTreeName());
        dto.setParentMenuId(table.getParentMenuId());
        dto.setParentMenuName(table.getParentMenuName());
        dto.setCreateBy(table.getCreateBy());
        dto.setCreateTime(table.getCreateTime());
        dto.setUpdateBy(table.getUpdateBy());
        dto.setUpdateTime(table.getUpdateTime());
        dto.setRemark(table.getRemark());
        return dto;
    }

    /**
     * Convert GenTableDto to GenTable entity
     */
    private GenTable convertToTableEntity(GenTableDto dto) {
        if (dto == null) return null;
        GenTable table = new GenTable();
        table.setTableId(dto.getTableId());
        table.setTableName(dto.getTableName());
        table.setTableComment(dto.getTableComment());
        table.setSubTableName(dto.getSubTableName());
        table.setSubTableFkName(dto.getSubTableFkName());
        table.setClassName(dto.getClassName());
        table.setTplCategory(dto.getTplCategory());
        table.setPackageName(dto.getPackageName());
        table.setModuleName(dto.getModuleName());
        table.setBusinessName(dto.getBusinessName());
        table.setFunctionName(dto.getFunctionName());
        table.setFunctionAuthor(dto.getFunctionAuthor());
        table.setFormColNum(dto.getFormColNum());
        table.setGenType(dto.getGenType());
        table.setGenPath(dto.getGenPath());
        table.setOptions(dto.getOptions());
        table.setTreeCode(dto.getTreeCode());
        table.setTreeParentCode(dto.getTreeParentCode());
        table.setTreeName(dto.getTreeName());
        table.setParentMenuId(dto.getParentMenuId());
        table.setParentMenuName(dto.getParentMenuName());
        table.setCreateBy(dto.getCreateBy());
        table.setCreateTime(dto.getCreateTime());
        table.setUpdateBy(dto.getUpdateBy());
        table.setUpdateTime(dto.getUpdateTime());
        table.setRemark(dto.getRemark());
        return table;
    }

    /**
     * Convert GenTableColumn entity to GenTableColumnDto
     */
    private GenTableColumnDto convertToColumnDto(GenTableColumn column) {
        if (column == null) return null;
        GenTableColumnDto dto = new GenTableColumnDto();
        dto.setColumnId(column.getColumnId());
        dto.setTableId(column.getTableId());
        dto.setColumnName(column.getColumnName());
        dto.setColumnComment(column.getColumnComment());
        dto.setColumnType(column.getColumnType());
        dto.setJavaType(column.getJavaType());
        dto.setJavaField(column.getJavaField());
        dto.setIsPk(column.getIsPk());
        dto.setIsIncrement(column.getIsIncrement());
        dto.setIsRequired(column.getIsRequired());
        dto.setIsInsert(column.getIsInsert());
        dto.setIsEdit(column.getIsEdit());
        dto.setIsList(column.getIsList());
        dto.setIsQuery(column.getIsQuery());
        dto.setQueryType(column.getQueryType());
        dto.setHtmlType(column.getHtmlType());
        dto.setDictType(column.getDictType());
        dto.setSort(column.getSort());
        dto.setCreateBy(column.getCreateBy());
        dto.setCreateTime(column.getCreateTime());
        dto.setUpdateBy(column.getUpdateBy());
        dto.setUpdateTime(column.getUpdateTime());
        return dto;
    }

    /**
     * Convert GenTableColumnDto to GenTableColumn entity
     */
    private GenTableColumn convertToColumnEntity(GenTableColumnDto dto) {
        if (dto == null) return null;
        GenTableColumn column = new GenTableColumn();
        column.setColumnId(dto.getColumnId());
        column.setTableId(dto.getTableId());
        column.setColumnName(dto.getColumnName());
        column.setColumnComment(dto.getColumnComment());
        column.setColumnType(dto.getColumnType());
        column.setJavaType(dto.getJavaType());
        column.setJavaField(dto.getJavaField());
        column.setIsPk(dto.getIsPk());
        column.setIsIncrement(dto.getIsIncrement());
        column.setIsRequired(dto.getIsRequired());
        column.setIsInsert(dto.getIsInsert());
        column.setIsEdit(dto.getIsEdit());
        column.setIsList(dto.getIsList());
        column.setIsQuery(dto.getIsQuery());
        column.setQueryType(dto.getQueryType());
        column.setHtmlType(dto.getHtmlType());
        column.setDictType(dto.getDictType());
        column.setSort(dto.getSort());
        column.setCreateBy(dto.getCreateBy());
        column.setCreateTime(dto.getCreateTime());
        column.setUpdateBy(dto.getUpdateBy());
        column.setUpdateTime(dto.getUpdateTime());
        return column;
    }
}
