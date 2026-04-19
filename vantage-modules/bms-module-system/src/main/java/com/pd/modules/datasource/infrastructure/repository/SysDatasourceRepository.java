package com.pd.modules.datasource.infrastructure.repository;

import com.pd.modules.datasource.domain.SysDatasource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysDatasourceRepository extends JpaRepository<SysDatasource, Long> {

    @Query("SELECT d FROM SysDatasource d WHERE d.status = '0' ORDER BY d.datasourceName")
    List<SysDatasource> findAllActive();

    Optional<SysDatasource> findByDatasourceKey(String datasourceKey);

    boolean existsByDatasourceKey(String datasourceKey);
}
