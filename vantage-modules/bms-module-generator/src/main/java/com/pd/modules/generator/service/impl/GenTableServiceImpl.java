package com.pd.modules.generator.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pd.modules.generator.domain.GenTable;
import com.pd.modules.generator.domain.GenTableColumn;
import com.pd.modules.generator.infrastructure.repository.GenTableColumnRepository;
import com.pd.modules.generator.infrastructure.repository.GenTableRepository;
import com.pd.modules.generator.service.IGenTableService;

@Service
public class GenTableServiceImpl implements IGenTableService {

    @Autowired
    private GenTableRepository genTableRepository;

    @Autowired
    private GenTableColumnRepository genTableColumnRepository;

    @Override
    public List<GenTable> selectGenTableList(GenTable genTable) {
        return genTableRepository.findByCondition(genTable.getTableName(), genTable.getTableComment());
    }

    @Override
    public List<GenTable> selectDbTableList(GenTable genTable) {
        return List.of();
    }

    @Override
    public GenTable selectGenTableById(Long tableId) {
        return genTableRepository.findById(tableId).orElse(null);
    }

    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return genTableColumnRepository.findByTableIdOrderBySort(tableId);
    }

    @Override
    public void importGenTable(String tables) {
    }

    @Override
    @Transactional
    public int updateGenTable(GenTable genTable) {
        genTableRepository.save(genTable);
        return 1;
    }

    @Override
    @Transactional
    public int deleteGenTableByIds(Long[] tableIds) {
        for (Long tableId : tableIds) {
            genTableRepository.deleteById(tableId);
            genTableColumnRepository.deleteByTableId(tableId);
        }
        return tableIds.length;
    }

    @Override
    public byte[] exportTableData(String tableName) {
        return new byte[0];
    }

    @Override
    public byte[] downloadZipData(String tableName) {
        return new byte[0];
    }

    @Override
    public Map<String, String> getTemplatePath(String tableName) {
        return Map.of();
    }

    @Override
    public String getPackagePath(String tableName) {
        return "";
    }

    @Override
    public String getProjectSourcePath() {
        return "";
    }
}