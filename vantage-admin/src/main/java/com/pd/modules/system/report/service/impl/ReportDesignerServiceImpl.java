package com.pd.modules.system.report.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pd.modules.system.datasource.domain.SysDatasource;
import com.pd.modules.system.datasource.infrastructure.repository.SysDatasourceRepository;
import com.pd.modules.system.report.api.ReportDesignerService;
import com.pd.modules.system.report.domain.SysReportTemplate;
import com.pd.modules.system.report.infrastructure.repository.SysReportTemplateRepository;
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

@Service("reportDesignerService")
public class ReportDesignerServiceImpl implements ReportDesignerService {

    private static final Logger log = LoggerFactory.getLogger(ReportDesignerServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final SysReportTemplateRepository templateRepository;
    private final SysDatasourceRepository datasourceRepository;
    private final JdbcTemplate jdbcTemplate;

    public ReportDesignerServiceImpl(SysReportTemplateRepository templateRepository,
                                  SysDatasourceRepository datasourceRepository,
                                  JdbcTemplate jdbcTemplate) {
        this.templateRepository = templateRepository;
        this.datasourceRepository = datasourceRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SysReportTemplate> findAll() {
        return templateRepository.findAllActive();
    }

    public List<SysReportTemplate> findAllVersions() {
        return templateRepository.findAll();
    }

    public List<Map<String, Object>> getJobsUsingTemplate(Long templateId) {
        return Collections.emptyList();
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
        if (template.getTemplateId() == null) {
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

    public String buildSqlFromTemplate(SysReportTemplate template) {
        return buildSqlFromTemplateInternal(template);
    }

    private String buildSqlFromTemplateInternal(SysReportTemplate template) {
        if (("SQL".equals(template.getReportMode()) || "HYBRID".equals(template.getReportMode())) 
                && template.getSqlContent() != null && !template.getSqlContent().trim().isEmpty()) {
            return template.getSqlContent().trim();
        }

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
                JsonNode firstCol = columns.get(0);
                if (firstCol.has("tableName")) {
                    sql.append(" FROM ").append(firstCol.get("tableName").asText());
                }
            }

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

    public List<Map<String, Object>> executeTemplate(Long templateId, String paramsJson) {
        Optional<SysReportTemplate> templateOpt = templateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            throw new RuntimeException("Report template not found: " + templateId);
        }

        SysReportTemplate template = templateOpt.get();
        String sql = buildSqlFromTemplateInternal(template);
        sql = replaceDynamicVariables(sql);

        if (paramsJson != null && !paramsJson.trim().isEmpty() && !paramsJson.equals("{}")) {
            try {
                JsonNode params = objectMapper.readTree(paramsJson);
                if (params.isObject() && params.size() > 0) {
                    Iterator<Map.Entry<String, JsonNode>> fields = params.fields();
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
        } else {
            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");
            java.util.regex.Matcher paramMatcher = paramPattern.matcher(sql);
            while (paramMatcher.find()) {
                sql = sql.replace(":" + paramMatcher.group(1), "NULL");
            }
        }

        log.info("Executing report template {}: {}", template.getTemplateName(), sql);
        return executeQuery(template.getDatasourceKey(), sql);
    }

    private String replaceDynamicVariables(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDateTime now_dt = java.time.LocalDateTime.now();

        java.util.regex.Pattern sysdatePattern = java.util.regex.Pattern.compile("\\$\\{SYSDATE(?::([^}]+))?\\}");
        java.util.regex.Matcher sysdateMatcher = sysdatePattern.matcher(sql);
        while (sysdateMatcher.find()) {
            String format = sysdateMatcher.group(1);
            String value;
            if (format != null) {
                value = now.format(java.time.format.DateTimeFormatter.ofPattern(format));
            } else {
                value = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            sql = sysdateMatcher.replaceAll("'" + value + "'");
            sysdateMatcher = sysdatePattern.matcher(sql);
        }

        java.util.regex.Pattern sysdtPattern = java.util.regex.Pattern.compile("\\$\\{SYSDATETIME(?::([^}]+))?\\}");
        java.util.regex.Matcher sysdtMatcher = sysdtPattern.matcher(sql);
        while (sysdtMatcher.find()) {
            String format = sysdtMatcher.group(1);
            String value;
            if (format != null) {
                value = now_dt.format(java.time.format.DateTimeFormatter.ofPattern(format));
            } else {
                value = now_dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            sql = sysdtMatcher.replaceAll("'" + value + "'");
            sysdtMatcher = sysdtPattern.matcher(sql);
        }

        sql = sql.replace("${YEAR}", String.valueOf(now.getYear()));
        int month = now.getMonthValue();
        sql = sql.replace("${MONTH}", String.format("%02d", month));
        int day = now.getDayOfMonth();
        sql = sql.replace("${DAY}", String.format("%02d", day));

        java.time.LocalDate prevDay = now.minusDays(1);
        sql = sql.replace("${PREV_DAY}", prevDay.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        java.time.LocalDate nextDay = now.plusDays(1);
        sql = sql.replace("${NEXT_DAY}", nextDay.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return sql;
    }

    public List<Map<String, Object>> executeQuery(String datasourceKey, String sql) {
        Optional<SysDatasource> dsOpt = datasourceRepository.findByDatasourceKey(datasourceKey);
        if (dsOpt.isEmpty()) {
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
            writer.println(String.join(",", data.get(0).keySet()));
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
        response.setContentType("application/vnd.ms-excel; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + reportName + ".xls\"");

        PrintWriter writer = response.getWriter();
        if (!data.isEmpty()) {
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

    public byte[] generateReportAttachment(Long templateId, String paramsJson, String format) throws IOException {
        List<Map<String, Object>> data = executeTemplate(templateId, paramsJson);
        Optional<SysReportTemplate> templateOpt = templateRepository.findById(templateId);
        String reportName = templateOpt.map(SysReportTemplate::getTemplateName).orElse("Report");

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
