package com.pd.modules.report.infrastructure.repository;

import com.pd.modules.report.domain.SysReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysReportTemplateRepository extends JpaRepository<SysReportTemplate, Long> {

    @Query("SELECT r FROM SysReportTemplate r WHERE r.status = '0' ORDER BY r.createTime DESC")
    List<SysReportTemplate> findAllActive();

    Optional<SysReportTemplate> findByTemplateKey(String templateKey);

    boolean existsByTemplateKey(String templateKey);

    @Query("SELECT r FROM SysReportTemplate r WHERE r.datasourceKey = :datasourceKey AND r.status = '0' ORDER BY r.version DESC")
    List<SysReportTemplate> findByDatasourceKey(@Param("datasourceKey") String datasourceKey);

    @Query("SELECT r FROM SysReportTemplate r WHERE r.templateKey = :templateKey AND r.status = '0' ORDER BY r.version DESC")
    List<SysReportTemplate> findByTemplateKeyOrderByVersionDesc(@Param("templateKey") String templateKey);

    @Query("SELECT MAX(r.version) FROM SysReportTemplate r WHERE r.templateKey = :templateKey")
    Integer findMaxVersionByTemplateKey(@Param("templateKey") String templateKey);
}
