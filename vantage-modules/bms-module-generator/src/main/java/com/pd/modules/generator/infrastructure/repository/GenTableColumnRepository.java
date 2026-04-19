package com.pd.modules.generator.infrastructure.repository;

import com.pd.modules.generator.domain.GenTableColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenTableColumnRepository extends JpaRepository<GenTableColumn, Long> {
    List<GenTableColumn> findByTableIdOrderBySort(Long tableId);
    void deleteByTableId(Long tableId);
    List<GenTableColumn> findByTableId(Long tableId);
}