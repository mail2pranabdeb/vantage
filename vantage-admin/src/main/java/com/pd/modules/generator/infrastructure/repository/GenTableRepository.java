package com.pd.modules.generator.infrastructure.repository;

import com.pd.modules.generator.domain.GenTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenTableRepository extends JpaRepository<GenTable, Long> {

    List<GenTable> findAll();

    @Query("SELECT t FROM GenTable t WHERE " +
           "(:tableName IS NULL OR t.tableName LIKE %:tableName%) AND " +
           "(:tableComment IS NULL OR t.tableComment LIKE %:tableComment%)")
    List<GenTable> findByCondition(@Param("tableName") String tableName,
                                  @Param("tableComment") String tableComment);

    Optional<GenTable> findByTableName(String tableName);

    Optional<GenTable> findById(Long tableId);

    void deleteById(Long tableId);

    <S extends GenTable> S save(S entity);
}