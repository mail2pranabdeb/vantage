package com.pd.modules.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.modules.datasource.domain.SysDatasource;
import com.pd.modules.datasource.infrastructure.repository.SysDatasourceRepository;
import com.pd.modules.report.domain.SysReportTemplate;
import com.pd.modules.report.infrastructure.repository.SysReportTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for report designer functionality.
 * Handles template CRUD, SQL generation from visual builder, report execution, and export.
 */
@Service
public class ReportDesignerService {

    private static final Logger log = LoggerFactory.getLogger(ReportDesignerService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final SysReportTemplateRepository templateRepository;
    private final SysDatasourceRepository datasourceRepository;
    private final JdbcTemplate jdbcTemplate;

    public ReportDesignerService(SysReportTemplateRepository templateRepository,
                                 SysDatasourceRepository datasourceRepository,
                                 JdbcTemplate jdbcTemplate) {
        this.templateRepository = templateRepository;
        this.datasourceRepository = datasourceRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== Template CRUD ====================

    public List<SysReportTemplate> findAll() {
        return templateRepository.findAllActive();
    }

    public Optional<SysReportTemplate> findById(Long templateId) {
        return templateRepository.findById(templateId);
    }

    public Optional<SysReportTemplate> findByTemplateKey(String templateKey) {
        return templateRepository.findByTemplateKey(templateKey);
    }

    public List<SysReportTemplate> findByTemplateKeyOrderByVersionDesc(String templateKey) {
        return templateRepository.findByTemplateKeyOrderByVersionDesc(templateKey);
    }

    @Transactional
    public SysReportTemplate save(SysReportTemplate template) {
        // Auto-increment version when editing existing template
        if (template.getTemplateId() != null) {
            Optional<SysReportTemplate> existing = templateRepository.findById(template.getTemplateId());
            if (existing.isPresent()) {
                SysReportTemplate existingTpl = existing.get();
                Integer maxVersion = templateRepository.findMaxVersionByTemplateKey(existingTpl.getTemplateKey());
                template.setVersion((maxVersion != null ? maxVersion : 0) + 1);
                template.setParentTemplateId(existingTpl.getParentTemplateId() != null ? existingTpl.getParentTemplateId() : existingTpl.getTemplateId());
                template.setCreateTime(existingTpl.getCreateTime());
                template.setCreateBy(existingTpl.getCreateBy());
            }
        } else {
            template.setVersion(1);
        }
        template.setUpdateTime(LocalDateTime.now());
        return templateRepository.save(template);
    }

    @Transactional
    public boolean deleteById(Long templateId) {
        if (templateRepository.existsById(templateId)) {
            templateRepository.deleteById(templateId);
            return true;
        }
        return false;
    }

    // ==================== Datasource Metadata ====================

    /**
     * Get tables and columns from a datasource
     */
    public List<Map<String, Object>> getDatasourceTables(String datasourceKey) {
        Optional<SysDatasource> dsOpt = datasourceRepository.findByDatasourceKey(datasourceKey);
        if (dsOpt.isEmpty()) {
            return Collections.emptyList();
        }

        SysDatasource ds = dsOpt.get();
        List<Map<String, Object>> tables = new ArrayList<>();

        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(ds.getDriverClass());
            dataSource.setUrl(ds.getUrl());
            dataSource.setUsername(ds.getUsername());
            dataSource.setPassword(ds.getPassword());

            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        Map<String, Object> table = new LinkedHashMap<>();
                        String tableName = rs.getString("TABLE_NAME");
                        table.put("tableName", tableName);
                        table.put("tableComment", rs.getString("REMARKS"));

                        // Get columns
                        List<Map<String, Object>> columns = new ArrayList<>();
                        try (ResultSet cols = meta.getColumns(null, null, tableName, null)) {
                            while (cols.next()) {
                                Map<String, Object> col = new LinkedHashMap<>();
                                col.put("columnName", cols.getString("COLUMN_NAME"));
                                col.put("dataType", cols.getString("TYPE_NAME"));
                                col.put("nullable", "YES".equals(cols.getString("IS_NULLABLE")));
                                col.put("remarks", cols.getString("REMARKS"));
                                columns.add(col);
                            }
                        }
                        table.put("columns", columns);
                        tables.add(table);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch tables from datasource {}: {}", datasourceKey, e.getMessage());
            return Collections.emptyList();
        }

        return tables;
    }

    // ==================== SQL Builder ====================

    /**
     * Build SQL from visual builder configuration
     */
    public String buildSqlFromTemplate(SysReportTemplate template) {
        // If SQL mode has content, use it directly
        if (("SQL".equals(template.getReportMode()) || "HYBRID".equals(template.getReportMode())) 
                && template.getSqlContent() != null && !template.getSqlContent().trim().isEmpty()) {
            return template.getSqlContent().trim();
        }

        // Build SQL from visual column config
        try {
            if (template.getColumnsConfig() == null || template.getColumnsConfig().equals("[]") || template.getColumnsConfig().trim().isEmpty()) {
                throw new IllegalArgumentException("No columns configured. Please add columns in the Columns tab or write SQL in the SQL tab.");
            }

            StringBuilder sql = new StringBuilder("SELECT ");
            JsonNode columns = objectMapper.readTree(template.getColumnsConfig());
            
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("No columns configured. Please add columns in the Columns tab.");
            }

            List<String> selectCols = new ArrayList<>();
            for (JsonNode col : columns) {
                String expr = col.has("expression") && !col.get("expression").asText().isEmpty() 
                    ? col.get("expression").asText() 
                    : col.get("columnName").asText();
                String alias = col.has("alias") && !col.get("alias").asText().isEmpty() ? col.get("alias").asText() : null;
                if (alias != null && !alias.equals(expr)) {
                    selectCols.add(expr + " AS " + alias);
                } else {
                    selectCols.add(expr);
                }
            }
            sql.append(String.join(", ", selectCols));

            // FROM
            if (template.getTablesConfig() != null && !template.getTablesConfig().equals("[]")) {
                JsonNode tables = objectMapper.readTree(template.getTablesConfig());
                if (tables.isArray() && tables.size() > 0) {
                    sql.append(" FROM ").append(tables.get(0).get("tableName").asText());
                    for (int i = 1; i < tables.size(); i++) {
                        JsonNode table = tables.get(i);
                        if (table.has("joinType") && table.has("joinCondition")) {
                            sql.append(" ").append(table.get("joinType").asText()).append(" JOIN ")
                               .append(table.get("tableName").asText())
                               .append(" ON ").append(table.get("joinCondition").asText());
                        }
                    }
                }
            } else {
                // Use table from first column
                JsonNode firstCol = columns.get(0);
                if (firstCol.has("tableName")) {
                    sql.append(" FROM ").append(firstCol.get("tableName").asText());
                }
            }

            // WHERE
            if (template.getFiltersConfig() != null && !template.getFiltersConfig().equals("[]")) {
                JsonNode filters = objectMapper.readTree(template.getFiltersConfig());
                List<String> conditions = new ArrayList<>();
                for (JsonNode filter : filters) {
                    if (filter.has("enabled") && !filter.get("enabled").asBoolean()) continue;
                    String column = filter.get("column").asText();
                    String operator = filter.get("operator").asText();
                    String value = filter.has("paramName") ? ":" + filter.get("paramName").asText() : filter.get("value").asText();
                    conditions.add(column + " " + operator + " " + value);
                }
                if (!conditions.isEmpty()) {
                    sql.append(" WHERE ").append(String.join(" AND ", conditions));
                }
            }

            // GROUP BY
            if (template.getGroupByConfig() != null && !template.getGroupByConfig().equals("[]")) {
                JsonNode groups = objectMapper.readTree(template.getGroupByConfig());
                List<String> groupCols = new ArrayList<>();
                for (JsonNode g : groups) {
                    groupCols.add(g.get("column").asText());
                }
                if (!groupCols.isEmpty()) {
                    sql.append(" GROUP BY ").append(String.join(", ", groupCols));
                }
            }

            // ORDER BY
            if (template.getOrderByConfig() != null && !template.getOrderByConfig().equals("[]")) {
                JsonNode orders = objectMapper.readTree(template.getOrderByConfig());
                List<String> orderParts = new ArrayList<>();
                for (JsonNode o : orders) {
                    orderParts.add(o.get("column").asText() + " " + o.get("direction").asText());
                }
                if (!orderParts.isEmpty()) {
                    sql.append(" ORDER BY ").append(String.join(", ", orderParts));
                }
            }

            log.info("Generated SQL from template: {}", sql);
            return sql.toString();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to build SQL from template", e);
            throw new RuntimeException("Failed to build SQL from visual configuration: " + e.getMessage());
        }
    }

    // ==================== Report Execution ====================

    /**
     * Execute a report template with optional parameters
     */
    public List<Map<String, Object>> executeTemplate(Long templateId, String paramsJson) {
        Optional<SysReportTemplate> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Report template not found: " + templateId);
        }

        SysReportTemplate template = templateOpt.get();
        String sql = buildSqlFromTemplate(template);

        // Replace parameters in SQL
        if (paramsJson != null && !paramsJson.isEmpty()) {
            try {
                JsonNode params = objectMapper.readTree(paramsJson);
                if (params.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = params.properties().iterator();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String placeholder = ":" + field.getKey();
                        String value = field.getValue().asText();
                        sql = sql.replace(placeholder, "'" + value.replace("'", "''") + "'");
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse params JSON", e);
            }
        }

        log.info("Executing report template {}: {}", template.getTemplateName(), sql);

        // Execute using the template's datasource
        return executeQuery(template.getDatasourceKey(), sql);
    }

    /**
     * Execute SQL on a specific datasource
     */
    public List<Map<String, Object>> executeQuery(String datasourceKey, String sql) {
        Optional<SysDatasource> dsOpt = datasourceRepository.findByDatasourceKey(datasourceKey);
        if (dsOpt.isEmpty()) {
            // Fallback to default JdbcTemplate
            log.warn("Datasource {} not found, using default", datasourceKey);
            return jdbcTemplate.queryForList(sql);
        }

        SysDatasource ds = dsOpt.get();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(ds.getDriverClass());
        dataSource.setUrl(ds.getUrl());
        dataSource.setUsername(ds.getUsername());
        dataSource.setPassword(ds.getPassword());

        JdbcTemplate dsJdbcTemplate = new JdbcTemplate(dataSource);
        return dsJdbcTemplate.queryForList(sql);
    }

    // ==================== Export ====================

    /**
     * Export report data to specified format
     */
    public void exportReport(Long templateId, String paramsJson, String format, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = executeTemplate(templateId, paramsJson);
        Optional<SysReportTemplate> templateOpt = templateRepository.findById(templateId);
        String reportName = templateOpt.map(SysReportTemplate::getTemplateName).orElse("Report");

        switch (format.toUpperCase()) {
            case "CSV":
                exportCsv(reportName, data, response);
                break;
            case "JSON":
                exportJson(reportName, data, response);
                break;
            case "HTML":
                exportHtml(reportName, data, response);
                break;
            case "EXCEL":
                exportExcel(reportName, data, response);
                break;
            default:
                exportExcel(reportName, data, response);
        }
    }

    private void exportCsv(String reportName, List<Map<String, Object>> data, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + ".csv\"");

        PrintWriter writer = response.getWriter();
        if (!data.isEmpty()) {
            // Header
            writer.println(String.join(",", data.get(0).keySet()));
            // Data
            for (Map<String, Object> row : data) {
                List<String> values = row.values().stream()
                    .map(v -> v != null ? "\"" + v.toString().replace("\"", "\"\"") + "\"" : "")
                    .collect(Collectors.toList());
                writer.println(String.join(",", values));
            }
        }
        writer.flush();
    }

    private void exportJson(String reportName, List<Map<String, Object>> data, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + ".json\"");

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getWriter(), data);
    }

    private void exportHtml(String reportName, List<Map<String, Object>> data, HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + ".html\"");

        PrintWriter writer = response.getWriter();
        writer.println("<html><head><title>" + reportName + "</title>");
        writer.println("<style>table{border-collapse:collapse;width:100%}th,td{border:1px solid #ddd;padding:8px;text-align:left}th{background:#4CAF50;color:white}</style>");
        writer.println("</head><body><h2>" + reportName + "</h2>");
        writer.println("<table><tr>");

        if (!data.isEmpty()) {
            for (String col : data.get(0).keySet()) {
                writer.println("<th>" + col + "</th>");
            }
            writer.println("</tr>");
            for (Map<String, Object> row : data) {
                writer.println("<tr>");
                for (Object val : row.values()) {
                    writer.println("<td>" + (val != null ? val : "") + "</td>");
                }
                writer.println("</tr>");
            }
        }
        writer.println("</table></body></html>");
        writer.flush();
    }

    private void exportExcel(String reportName, List<Map<String, Object>> data, HttpServletResponse response) throws IOException {
        // For now, export as CSV with .xls extension (proper Excel requires Apache POI)
        response.setContentType("application/vnd.ms-excel; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + ".xls\"");

        PrintWriter writer = response.getWriter();
        if (!data.isEmpty()) {
            // Tab-separated for Excel compatibility
            writer.println(String.join("\t", data.get(0).keySet()));
            for (Map<String, Object> row : data) {
                List<String> values = row.values().stream()
                    .map(v -> v != null ? v.toString() : "")
                    .collect(Collectors.toList());
                writer.println(String.join("\t", values));
            }
        }
        writer.flush();
    }

    // ==================== Email Integration ====================

    /**
     * Execute template and prepare email attachment
     * Returns byte array of the report in specified format
     */
    public byte[] generateReportAttachment(Long templateId, String paramsJson, String format) throws IOException {
        List<Map<String, Object>> data = executeTemplate(templateId, paramsJson);
        Optional<SysReportTemplate> templateOpt = templateRepository.findById(templateId);
        String reportName = templateOpt.map(SysReportTemplate::getTemplateName).orElse("Report");

        // Generate based on format
        switch (format.toUpperCase()) {
            case "CSV":
                return generateCsvBytes(reportName, data);
            case "JSON":
                return objectMapper.writeValueAsBytes(data);
            case "HTML":
                return generateHtmlBytes(reportName, data);
            case "EXCEL":
            default:
                return generateExcelBytes(reportName, data);
        }
    }

    private byte[] generateCsvBytes(String reportName, List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        if (!data.isEmpty()) {
            sb.append(String.join(",", data.get(0).keySet())).append("\n");
            for (Map<String, Object> row : data) {
                List<String> values = row.values().stream()
                    .map(v -> v != null ? "\"" + v.toString().replace("\"", "\"\"") + "\"" : "")
                    .collect(Collectors.toList());
                sb.append(String.join(",", values)).append("\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
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
        return html.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateExcelBytes(String reportName, List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        if (!data.isEmpty()) {
            sb.append(String.join("\t", data.get(0).keySet())).append("\n");
            for (Map<String, Object> row : data) {
                List<String> values = row.values().stream()
                    .map(v -> v != null ? v.toString() : "")
                    .collect(Collectors.toList());
                sb.append(String.join("\t", values)).append("\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get template info for email job
     */
    public Map<String, Object> getTemplateInfoForEmail(Long templateId) {
        Optional<SysReportTemplate> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        SysReportTemplate template = templateOpt.get();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("templateId", template.getTemplateId());
        info.put("templateName", template.getTemplateName());
        info.put("outputFormat", template.getOutputFormat());
        return info;
    }
}
