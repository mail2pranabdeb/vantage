package com.pd.modules.system.api;

import com.pd.modules.system.report.domain.SysReport;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * System module public API for report operations.
 */
public interface SystemReportService {

    List<SysReport> findAll();

    Optional<SysReport> findById(Long reportId);

    SysReport save(SysReport report);

    void deleteById(Long reportId);

    boolean existsByReportKey(String reportKey);

    List<Map<String, Object>> executeReport(Long reportId, String params);

    void downloadReport(Object response, SysReport report, List<Map<String, Object>> data, String format) throws Exception;
}
