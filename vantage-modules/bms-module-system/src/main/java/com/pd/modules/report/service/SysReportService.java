package com.pd.modules.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.modules.report.domain.SysReport;
import com.pd.modules.report.infrastructure.repository.SysReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.ByteArrayResource;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SysReportService {

    private static final Logger log = LoggerFactory.getLogger(SysReportService.class);

    @Autowired
    private SysReportRepository reportRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
        List<Map<String, Object>> results = executeReport(reportId, null);
        
        // Send email if enabled
        if (report.getEmailEnabled() && report.getEmailRecipients() != null && mailSender != null) {
            sendReportEmail(report, results);
        }
    }

    /**
     * Execute report and return results
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> executeReport(Long reportId, String params) {
        Optional<SysReport> reportOpt = reportRepository.findById(reportId);
        if (!reportOpt.isPresent()) {
            throw new RuntimeException("Report not found: " + reportId);
        }

        SysReport report = reportOpt.get();
        
        if (!"0".equals(report.getStatus())) {
            throw new RuntimeException("Report is disabled: " + report.getReportName());
        }

        try {
            // Execute SQL with JdbcTemplate
            List<Map<String, Object>> results = executeSql(report.getSqlContent(), params);
            
            // Log execution
            logExecution(reportId, report.getReportName(), params, results.size(), "0", null);
            
            return results;
        } catch (Exception e) {
            logExecution(reportId, report.getReportName(), params, 0, "1", e.getMessage());
            throw new RuntimeException("Failed to execute report: " + e.getMessage(), e);
        }
    }

    /**
     * Execute SQL query with parameter substitution
     */
    private List<Map<String, Object>> executeSql(String sql, String params) {
        if (jdbcTemplate == null) {
            throw new RuntimeException("JdbcTemplate not configured");
        }

        try {
            // Parse parameters and replace :paramName in SQL
            String finalSql = sql;
            if (params != null && !params.isEmpty() && !params.equals("{}")) {
                JsonNode paramNode = objectMapper.readTree(params);
                Iterator<Map.Entry<String, JsonNode>> fields = paramNode.properties().iterator();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String paramName = ":" + field.getKey();
                    String paramValue = field.getValue().asText();
                    finalSql = finalSql.replace(paramName, "'" + paramValue.replace("'", "''") + "'");
                }
            }

            // Execute query
            return jdbcTemplate.queryForList(finalSql);
        } catch (Exception e) {
            throw new RuntimeException("SQL execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Log report execution
     */
    @Transactional
    public void logExecution(Long reportId, String reportName, String params, int rowCount, String status, String errorMsg) {
        if (jdbcTemplate == null) return;
        
        try {
            jdbcTemplate.update(
                "INSERT INTO sys_report_exec (exec_id, report_id, report_name, exec_params, output_format, status, error_msg, exec_time) " +
                "VALUES (NEXT VALUE FOR sys_report_exec_seq, ?, ?, ?, 'EXCEL', ?, ?, CURRENT_TIMESTAMP)",
                reportId, reportName, params, status, errorMsg
            );
        } catch (Exception e) {
            // Ignore logging errors
        }
    }

    /**
     * Send report via email with Excel attachment
     */
    public void sendReportEmail(SysReport report, List<Map<String, Object>> results) {
        if (mailSender == null) {
            log.warn("Mail sender not configured, skipping email");
            return;
        }

        try {
            // Create Excel file in memory
            byte[] excelData = generateExcel(report.getReportName(), results);
            
            // Send email with attachment
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("reports@vantage.com");
            helper.setTo(report.getEmailRecipients().split(","));
            helper.setSubject(report.getEmailSubject() != null ? 
                report.getEmailSubject() : "Report: " + report.getReportName());
            
            String body = String.format(
                "<html><body>" +
                "<h2>Report: %s</h2>" +
                "<p>Executed at: %s</p>" +
                "<p>Results: %d rows</p>" +
                "<p>Please find the attached Excel file.</p>" +
                "</body></html>",
                report.getReportName(),
                LocalDateTime.now(),
                results.size()
            );
            helper.setText(body, true);
            
            // Attach Excel file
            helper.addAttachment(report.getReportKey() + ".xlsx", 
                new ByteArrayResource(excelData));
            
            mailSender.send(message);
            log.info("Report email with attachment sent to: {}", report.getEmailRecipients());
        } catch (Exception e) {
            log.error("Failed to send report email with attachment", e);
        }
    }

    /**
     * Generate Excel file from report data
     */
    private byte[] generateExcel(String reportName, List<Map<String, Object>> data) {
        try {
            // Simple CSV-like format for now (can be enhanced with Apache POI)
            StringBuilder sb = new StringBuilder();
            
            if (!data.isEmpty()) {
                // Headers
                sb.append(String.join(",", data.get(0).keySet())).append("\n");
                
                // Data rows
                for (Map<String, Object> row : data) {
                    List<String> values = row.values().stream()
                        .map(v -> v != null ? "\"" + v.toString().replace("\"", "\"\"") + "\"" : "")
                        .toList();
                    sb.append(String.join(",", values)).append("\n");
                }
            }
            
            return sb.toString().getBytes();
        } catch (Exception e) {
            log.error("Failed to generate Excel", e);
            return new byte[0];
        }
    }

    /**
     * Download report in specified format
     */
    public void downloadReport(jakarta.servlet.http.HttpServletResponse response,
                               SysReport report,
                               List<Map<String, Object>> data,
                               String format) throws Exception {
        byte[] fileData = generateExcel(report.getReportName(), data);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + report.getReportKey() + "." + format.toLowerCase() + "\"");
        response.setContentLength(fileData.length);
        response.getOutputStream().write(fileData);
        response.getOutputStream().flush();
    }

    /**
     * Generate report as byte array for email attachment
     */
    public byte[] generateReportAttachment(Long reportId, String params, String format) {
        try {
            List<Map<String, Object>> data = executeReport(reportId, params);
            Optional<SysReport> reportOpt = reportRepository.findById(reportId);
            String reportName = reportOpt.map(SysReport::getReportName).orElse("Report");

            if ("CSV".equalsIgnoreCase(format)) {
                return generateCsvBytes(reportName, data);
            } else if ("JSON".equalsIgnoreCase(format)) {
                return generateJsonBytes(data);
            } else if ("HTML".equalsIgnoreCase(format)) {
                return generateHtmlBytes(reportName, data);
            } else {
                return generateExcel(reportName, data);
            }
        } catch (Exception e) {
            log.error("Failed to generate report attachment", e);
            throw new RuntimeException("Failed to generate report attachment", e);
        }
    }

    private byte[] generateCsvBytes(String reportName, List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        if (!data.isEmpty()) {
            sb.append(String.join(",", data.get(0).keySet())).append("\n");
            for (Map<String, Object> row : data) {
                List<String> values = row.values().stream()
                    .map(v -> v != null ? "\"" + v.toString().replace("\"", "\"\"") + "\"" : "")
                    .toList();
                sb.append(String.join(",", values)).append("\n");
            }
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private byte[] generateJsonBytes(List<Map<String, Object>> data) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            log.error("Failed to generate JSON", e);
            return new byte[0];
        }
    }

    private byte[] generateHtmlBytes(String reportName, List<Map<String, Object>> data) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>").append(reportName).append("</title>");
        html.append("<style>table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px;text-align:left}th{background:#4CAF50;color:white}</style>");
        html.append("</head><body><h2>").append(reportName).append("</h2><table><tr>");
        if (!data.isEmpty()) {
            for (String col : data.get(0).keySet()) {
                html.append("<th>").append(col).append("</th>");
            }
            html.append("</tr>");
            for (Map<String, Object> row : data) {
                html.append("<tr>");
                for (Object val : row.values()) {
                    html.append("<td>").append(val != null ? val : "").append("</td>");
                }
                html.append("</tr>");
            }
        }
        html.append("</table></body></html>");
        return html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
