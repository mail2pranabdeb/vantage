package com.pd.modules.report.web;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.report.domain.SysReport;
import com.pd.modules.report.service.SysReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/report")
public class SysReportController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(SysReportController.class);

    @Autowired
    private SysReportService reportService;

    @PreAuthorize("hasAuthority('system:report:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<SysReport> reports = reportService.findAll();
        return success(reports);
    }

    @PreAuthorize("hasAuthority('system:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable Long reportId) {
        Optional<SysReport> report = reportService.findById(reportId);
        return report.map(this::success).orElseGet(() -> error("Report not found"));
    }

    @PreAuthorize("hasAuthority('system:report:add')")
    @Log(title = "Report Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysReport report) {
        if (reportService.existsByReportKey(report.getReportKey())) {
            return error("Report key already exists");
        }
        reportService.save(report);
        return success("Report added successfully");
    }

    @PreAuthorize("hasAuthority('system:report:edit')")
    @Log(title = "Report Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysReport report) {
        Optional<SysReport> existing = reportService.findById(report.getReportId());
        if (!existing.isPresent()) {
            return error("Report not found");
        }
        
        if (!existing.get().getReportKey().equals(report.getReportKey()) && 
            reportService.existsByReportKey(report.getReportKey())) {
            return error("Report key already exists");
        }
        
        reportService.save(report);
        return success("Report updated successfully");
    }

    @PreAuthorize("hasAuthority('system:report:remove')")
    @Log(title = "Report Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reportId}")
    public AjaxResult remove(@PathVariable Long reportId) {
        reportService.deleteById(reportId);
        return success("Report deleted successfully");
    }

    @PreAuthorize("hasAuthority('system:report:execute')")
    @Log(title = "Report Management", businessType = BusinessType.OTHER)
    @PostMapping("/execute/{reportId}")
    public AjaxResult execute(@PathVariable Long reportId, @RequestBody(required = false) Map<String, Object> params) {
        try {
            // Convert params Map to JSON string for parameter substitution
            String paramsJson = "{}";
            if (params != null && !params.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                paramsJson = mapper.writeValueAsString(params);
            }
            
            List<Map<String, Object>> results = reportService.executeReport(reportId, paramsJson);
            return success(results);
        } catch (Exception e) {
            log.error("Report execution failed", e);
            return error("Failed to execute report: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('system:report:execute')")
    @Log(title = "Report Management", businessType = BusinessType.EXPORT)
    @GetMapping("/download/{reportId}")
    public void download(@PathVariable Long reportId, 
                         @RequestParam(required = false) String params,
                         @RequestParam(required = false, defaultValue = "EXCEL") String format,
                         HttpServletResponse response) throws Exception {
        List<Map<String, Object>> data = reportService.executeReport(reportId, params);
        SysReport report = reportService.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        reportService.downloadReport(response, report, data, format);
    }
}
