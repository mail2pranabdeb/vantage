package com.pd.modules.report.service;

import com.pd.modules.report.domain.SysReport;
import com.pd.modules.report.infrastructure.repository.SysReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SysReportService {

    @Autowired
    private SysReportRepository reportRepository;

    public List<SysReport> findAll() {
        return reportRepository.findAllActive();
    }

    public Optional<SysReport> findById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    @Transactional
    public SysReport save(SysReport report) {
        if (report.getReportId() == null) {
            report.setCreateBy("admin");
            report.setCreateTime(LocalDateTime.now());
            report.setStatus("0");
        } else {
            report.setUpdateBy("admin");
            report.setUpdateTime(LocalDateTime.now());
        }
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteById(Long reportId) {
        reportRepository.deleteById(reportId);
    }

    public boolean existsByReportKey(String reportKey) {
        return reportRepository.existsByReportKey(reportKey);
    }

    /**
     * Execute report and return results as list of maps
     */
    @Transactional(readOnly = true)
    public List<Object> executeReport(Long reportId, String params) {
        // TODO: Implement report execution with JdbcTemplate
        // This would execute the SQL and return results
        return List.of();
    }
}
