package com.pd.modules.generator.service;

import java.util.List;
import com.pd.modules.generator.domain.GenTableColumn;

/**
 * Code generation table column service interface
 */
public interface IGenTableColumnService {

    /**
     * Get list of table columns
     */
    List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * Update table columns
     */
    int updateGenTableColumns(List<GenTableColumn> list);

    /**
     * Delete table columns by IDs
     */
    int deleteGenTableColumnByIds(Long[] columnIds);
}
