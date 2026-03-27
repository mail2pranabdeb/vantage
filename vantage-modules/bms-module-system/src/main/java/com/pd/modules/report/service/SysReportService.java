package com.pd.modules.report.service;

import com.pd.modules.report.domain.SysReport;
import com.pd.modules.report.infrastructure.repository.SysReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SysReportService {

    @Autowired
    private SysReportRepository reportRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

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
     * Execute report and send via email if configured
     */
    @Transactional
    public void executeAndEmail(Long reportId) {
        Optional<SysReport> reportOpt = reportRepository.findById(reportId);
        if (!reportOpt.isPresent()) return;

        SysReport report = reportOpt.get();
        
        // Execute report
        List<Object> results = executeReport(reportId, null);
        
        // Send email if enabled
        if (report.getEmailEnabled() && report.getEmailRecipients() != null && mailSender != null) {
            sendReportEmail(report, results);
        }
    }

    /**
     * Execute report (placeholder - would integrate with JdbcTemplate)
     */
    @Transactional(readOnly = true)
    public List<Object> executeReport(Long reportId, String params) {
        // TODO: Implement actual SQL execution with JdbcTemplate
        return List.of();
    }

    /**
     * Send report via email
     */
    private void sendReportEmail(SysReport report, List<Object> results) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("reports@vantage.com");
            message.setTo(report.getEmailRecipients().split(","));
            message.setSubject(report.getEmailSubject() != null ? 
                report.getEmailSubject() : "Report: " + report.getReportName());
            message.setText("Report executed successfully.\n\nResults: " + results.size() + " rows");
            
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
