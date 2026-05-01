package com.pd.modules.system.service.impl;

import com.pd.modules.system.report.domain.SysReport;
import com.pd.modules.system.report.service.SysReportService;
import com.pd.modules.system.api.SystemReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SystemReportServiceImpl implements SystemReportService {

    private final SysReportService reportService;

    public SystemReportServiceImpl(SysReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public List<SysReport> findAll() {
        return reportService.findAll();
    }

    @Override
    public Optional<SysReport> findById(Long reportId) {
        return reportService.findById(reportId);
    }

    @Override
    public SysReport save(SysReport report) {
        return reportService.save(report);
    }

    @Override
    public void deleteById(Long reportId) {
        reportService.deleteById(reportId);
    }

    @Override
    public boolean existsByReportKey(String reportKey) {
        return reportService.existsByReportKey(reportKey);
    }

    @Override
    public List<Map<String, Object>> executeReport(Long reportId, String params) {
        return reportService.executeReport(reportId, params);
    }

    @Override
    public void downloadReport(Object response, SysReport report, List<Map<String, Object>> data, String format) throws Exception {
        reportService.downloadReport((jakarta.servlet.http.HttpServletResponse) response, report, data, format);
    }
}
