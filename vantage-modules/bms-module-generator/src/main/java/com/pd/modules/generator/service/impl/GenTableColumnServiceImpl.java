package com.pd.modules.generator.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pd.modules.generator.domain.GenTableColumn;
import com.pd.modules.generator.infrastructure.repository.GenTableColumnRepository;
import com.pd.modules.generator.service.IGenTableColumnService;

/**
 * Code generation table column service implementation
 */
@Service
public class GenTableColumnServiceImpl implements IGenTableColumnService {

    @Autowired
    private GenTableColumnRepository genTableColumnRepository;

    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return genTableColumnRepository.findByTableId(tableId);
    }

    @Override
    public int updateGenTableColumns(List<GenTableColumn> list) {
        int count = 0;
        for (GenTableColumn column : list) {
            column.setUpdateTime(LocalDateTime.now());
            count += genTableColumnRepository.update(column);
        }
        return count;
    }

    @Override
    public int deleteGenTableColumnByIds(Long[] columnIds) {
        return genTableColumnRepository.deleteByIds(columnIds);
    }
}
