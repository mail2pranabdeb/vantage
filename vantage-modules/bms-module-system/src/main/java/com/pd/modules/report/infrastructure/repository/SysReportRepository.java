package com.pd.modules.report.infrastructure.repository;

import com.pd.modules.report.domain.SysReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysReportRepository extends JpaRepository<SysReport, Long> {

    @Query("SELECT r FROM SysReport r WHERE r.status = '0' ORDER BY r.reportName")
    List<SysReport> findAllActive();

    @Query("SELECT r FROM SysReport r WHERE r.reportKey = :reportKey")
    Optional<SysReport> findByReportKey(@Param("reportKey") String reportKey);

    boolean existsByReportKey(String reportKey);
}
