package com.pd.modules.quartz.util;

import com.pd.modules.quartz.domain.SysJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concrete Quartz job that parses invokeTarget string and executes via reflection.
 * 
 * Supported invokeTarget formats:
 * 1. beanName.methodName(arg1, arg2, ...) 
 *    Example: reportScheduleJob.execute(1, 'EXCEL', ['user@email.com'], null, 'Report', 'Body', '{}')
 * 
 * 2. beanName.methodName("stringArg", 123, true)
 *    Supports String, Long, Integer, Boolean, Double, and array arguments
 */
@Component("quartzTaskExecutor")
public class QuartzTaskExecutor extends AbstractQuartzJob {

    private static final Logger log = LoggerFactory.getLogger(QuartzTaskExecutor.class);

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Override
    protected void runTask(SysJob sysJob) throws Exception {
        // Check if it's a Report Job
        if ("REPORT".equals(sysJob.getJobType()) && sysJob.getReportId() != null) {
            executeReportJob(sysJob);
            return;
        }

        // ... existing Bean execution logic ...
        String invokeTarget = sysJob.getInvokeTarget();
        if (invokeTarget == null || invokeTarget.trim().isEmpty()) {
            throw new IllegalArgumentException("invokeTarget is empty");
        }

        // Parse: beanName.methodName(args)
        int dotIndex = invokeTarget.indexOf('.');
        int openParen = invokeTarget.indexOf('(');
        int closeParen = invokeTarget.lastIndexOf(')');

        if (dotIndex == -1 || openParen == -1 || closeParen == -1) {
            throw new IllegalArgumentException("Invalid invokeTarget format. Expected: beanName.methodName(args)");
        }

        String beanName = invokeTarget.substring(0, dotIndex).trim();
        String methodName = invokeTarget.substring(dotIndex + 1, openParen).trim();
        String argsStr = invokeTarget.substring(openParen + 1, closeParen).trim();

        // Get Spring bean
        Object bean = applicationContext.getBean(beanName);
        if (bean == null) {
            throw new IllegalArgumentException("Bean not found: " + beanName);
        }

        // Parse arguments
        Object[] args = parseArguments(argsStr);

        // Find and invoke method - try multiple type signatures for null args
        Method method = findMethod(bean.getClass(), methodName, args);
        if (method == null) {
            throw new IllegalArgumentException("Method not found: " + beanName + "." + methodName +
                " with compatible argument types");
        }

        log.info("Invoking {}.{} with {} arguments", beanName, methodName, args.length);
        method.invoke(bean, args);
    }

    /**
     * Logic to execute a Report Job
     */
    private void executeReportJob(SysJob sysJob) throws Exception {
        log.info("Executing Report Job ID: {}, Report ID: {}", sysJob.getJobId(), sysJob.getReportId());
        
        try {
            Object reportService = applicationContext.getBean("reportDesignerService");
            java.lang.reflect.Method findByIdMethod = reportService.getClass().getMethod("findById", Long.class);
            Optional<?> opt = (Optional<?>) findByIdMethod.invoke(reportService, sysJob.getReportId());
            
            if (opt.isPresent()) {
                Object template = opt.get();
                
                java.lang.reflect.Method getTemplateIdMethod = template.getClass().getMethod("getTemplateId");
                Long templateId = (Long) getTemplateIdMethod.invoke(template);
                
                java.lang.reflect.Method getTemplateNameMethod = template.getClass().getMethod("getTemplateName");
                String templateName = (String) getTemplateNameMethod.invoke(template);

                java.lang.reflect.Method getOutputFormatMethod = template.getClass().getMethod("getOutputFormat");
                String outputFormat = (String) getOutputFormatMethod.invoke(template);

                // Execute report
                java.lang.reflect.Method executeTemplateMethod = reportService.getClass().getMethod("executeTemplate", Long.class, String.class);
                List<?> data = (List<?>) executeTemplateMethod.invoke(reportService, templateId, "{}");

                // Send email
                sendReportEmail(sysJob, templateName, outputFormat, data);
            } else {
                throw new Exception("Report template not found: " + sysJob.getReportId());
            }
        } catch (Exception e) {
            log.error("Failed to execute report job", e);
            throw e;
        }
    }

    /**
     * Send report via email
     */
    private void sendReportEmail(SysJob sysJob, String templateName, String outputFormat, List<?> data) throws Exception {
        String recipients = sysJob.getNotificationEmails();
        String emailGroup = sysJob.getReportEmailGroup();
        
        // If email group is defined, resolve emails from dictionary by dict_code
        if (emailGroup != null && !emailGroup.isEmpty()) {
            try {
                // emailGroup stores dict codes comma-separated (e.g., "101,102")
                String[] dictCodes = emailGroup.split(",");
                StringBuilder groupEmails = new StringBuilder();
                
                Object dictRepo = applicationContext.getBean("sysDictDataRepository");
                java.lang.reflect.Method findByIdMethod = dictRepo.getClass().getMethod("findById", Long.class);
                
                for (String dictCodeStr : dictCodes) {
                    dictCodeStr = dictCodeStr.trim();
                    if (dictCodeStr.isEmpty()) continue;
                    
                    try {
                        Long dictCode = Long.parseLong(dictCodeStr);
                        java.util.Optional<?> optDict = (java.util.Optional<?>) findByIdMethod.invoke(dictRepo, dictCode);
                        if (optDict.isPresent()) {
                            Object dictData = optDict.get();
                            String dictValue = (String) dictData.getClass().getMethod("getDictValue").invoke(dictData);
                            if (dictValue != null && !dictValue.isEmpty()) {
                                if (groupEmails.length() > 0) groupEmails.append(",");
                                groupEmails.append(dictValue);
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Invalid dict_code in email group: {}", dictCodeStr);
                    }
                }
                
                if (groupEmails.length() > 0) {
                    if (recipients != null && !recipients.isEmpty()) {
                        recipients = recipients + "," + groupEmails;
                    } else {
                        recipients = groupEmails.toString();
                    }
                    log.info("Resolved email group codes '{}' to: {}", emailGroup, recipients);
                }
            } catch (Exception e) {
                log.error("Failed to resolve email group: {}", emailGroup, e);
            }
        }

        if (recipients == null || recipients.isEmpty()) {
            log.warn("No recipients configured for Job {}", sysJob.getJobId());
            return;
        }

        // Check if email template is configured
        if (sysJob.getEmailTemplateId() == null) {
            log.warn("No email template configured for Job {}. Skipping email.", sysJob.getJobId());
            return;
        }

        log.info("Sending {} report '{}' to {} using template ID: {}", outputFormat, templateName, recipients, sysJob.getEmailTemplateId());
        log.info("Data rows: {}", data.size());

        // Send the report email with attachment using the configured email template
        try {
            JavaMailSender mailSender = applicationContext.getBean(JavaMailSender.class);
            
            // Get the email template
            Object templateService = applicationContext.getBean("emailTemplateService");
            java.lang.reflect.Method getTemplateByIdMethod = templateService.getClass().getMethod("getTemplateById", Long.class);
            java.util.Optional<?> templateOpt = (java.util.Optional<?>) getTemplateByIdMethod.invoke(templateService, sysJob.getEmailTemplateId());

            if (templateOpt.isEmpty()) {
                log.error("Email template not found for ID: {}", sysJob.getEmailTemplateId());
                return;
            }

            Object template = templateOpt.get();
            java.lang.reflect.Method getSubjectMethod = template.getClass().getMethod("getEmailSubject");
            java.lang.reflect.Method getBodyMethod = template.getClass().getMethod("getEmailBody");
            java.lang.reflect.Method getDataTablesMethod = template.getClass().getMethod("getDataTables");

            String templateSubject = (String) getSubjectMethod.invoke(template);
            String templateBody = (String) getBodyMethod.invoke(template);
            String dataTablesJson = (String) getDataTablesMethod.invoke(template);

            // Process template variables
            String subject = processReportTemplate(templateSubject, templateName, outputFormat, data, sysJob);
            String body = processReportTemplate(templateBody, templateName, outputFormat, data, sysJob);

            // Execute multiple data table queries and render HTML tables if configured
            if (dataTablesJson != null && !dataTablesJson.isEmpty()) {
                try {
                    Object tplService = applicationContext.getBean("emailTemplateService");
                    java.lang.reflect.Method executeMultiQueryMethod = tplService.getClass().getMethod("executeMultipleQueriesAndRenderTables", String.class);
                    String dataTableHtml = (String) executeMultiQueryMethod.invoke(tplService, dataTablesJson);
                    body = body.replace("${dataTable}", dataTableHtml);
                } catch (Exception e) {
                    log.error("Failed to execute template data table queries", e);
                    body = body.replace("${dataTable}", "<p style='color:red;'>Error: Failed to execute data queries - " + e.getMessage() + "</p>");
                }
            } else {
                // Fallback: backward compatibility with single table fields
                java.lang.reflect.Method getDatasourceKeyMethod = template.getClass().getMethod("getDatasourceKey");
                java.lang.reflect.Method getQuerySqlMethod = template.getClass().getMethod("getQuerySql");
                java.lang.reflect.Method getIncludeDataTableMethod = template.getClass().getMethod("getIncludeDataTable");
                String datasourceKey = (String) getDatasourceKeyMethod.invoke(template);
                String querySql = (String) getQuerySqlMethod.invoke(template);
                Boolean includeDataTable = (Boolean) getIncludeDataTableMethod.invoke(template);

                if (Boolean.TRUE.equals(includeDataTable) && datasourceKey != null && !datasourceKey.isEmpty() && querySql != null && !querySql.isEmpty()) {
                    try {
                        Object tplService = applicationContext.getBean("emailTemplateService");
                        java.lang.reflect.Method executeQueryMethod = tplService.getClass().getMethod("executeQueryAndRenderTable", String.class, String.class);
                        String dataTableHtml = (String) executeQueryMethod.invoke(tplService, datasourceKey, querySql);
                        body = body.replace("${dataTable}", dataTableHtml);
                    } catch (Exception e) {
                        log.error("Failed to execute template SQL query", e);
                        body = body.replace("${dataTable}", "<p style='color:red;'>Error: Failed to execute data query - " + e.getMessage() + "</p>");
                    }
                } else {
                    body = body.replace("${dataTable}", "");
                }
            }

            // Send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Vantage Admin <noreply@vantage.com>");
            helper.setTo(recipients.split(","));
            helper.setSubject(subject);
            helper.setText(body, true);

            // Attach report as CSV
            String csvContent = convertToCsv(data);
            String fileName = templateName.replaceAll("[^a-zA-Z0-9\\s-]", "_") + "_" + 
                              java.time.LocalDate.now() + ".csv";
            helper.addAttachment(fileName, 
                new org.springframework.core.io.ByteArrayResource(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                "text/csv");

            mailSender.send(message);
            log.info("Report email with attachment sent successfully to {}", recipients);
        } catch (Exception e) {
            log.error("Failed to send report email to {}", recipients, e);
        }
    }

    /**
     * Process template variables
     */
    private String processReportTemplate(String template, String templateName, String outputFormat, List<?> data, SysJob sysJob) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String appName = "Vantage";

        return template
            .replace("${appName}", appName)
            .replace("${jobId}", String.valueOf(sysJob.getJobId()))
            .replace("${jobName}", sysJob.getJobName())
            .replace("${jobGroup}", sysJob.getJobGroup())
            .replace("${reportName}", templateName)
            .replace("${reportFormat}", outputFormat)
            .replace("${totalRows}", String.valueOf(data.size()))
            .replace("${executionTime}", LocalDateTime.now().format(formatter))
            .replace("${timestamp}", LocalDateTime.now().format(formatter));
    }

    /**
     * Convert report data to CSV format
     */
    private String convertToCsv(List<?> data) {
        StringBuilder csv = new StringBuilder();
        
        if (data.isEmpty()) {
            return "No data available\n";
        }

        Object firstRow = data.getFirst();
        switch (firstRow) {
            case java.util.Map<?, ?> firstMap -> {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) firstMap;
                List<String> headers = new ArrayList<>(map.keySet());
                
                // Write header
                csv.append(String.join(",", headers)).append("\n");
                
                // Write data rows
                for (Object row : data) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> rowMap = (java.util.Map<String, Object>) row;
                    List<String> values = new ArrayList<>();
                    for (String header : headers) {
                        Object val = rowMap.get(header);
                        String strVal = val != null ? val.toString() : "";
                        // Escape commas in values
                        if (strVal.contains(",") || strVal.contains("\"") || strVal.contains("\n")) {
                            strVal = "\"" + strVal.replace("\"", "\"\"") + "\"";
                        }
                        values.add(strVal);
                    }
                    csv.append(String.join(",", values)).append("\n");
                }
            }
            case Object[] firstArray -> {
                for (int i = 0; i < firstArray.length; i++) {
                    csv.append("Column").append(i + 1);
                    if (i < firstArray.length - 1) csv.append(",");
                }
                csv.append("\n");
                
                for (Object row : data) {
                    Object[] rowArray = (Object[]) row;
                    for (int i = 0; i < rowArray.length; i++) {
                        String val = rowArray[i] != null ? rowArray[i].toString() : "";
                        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
                            val = "\"" + val.replace("\"", "\"\"") + "\"";
                        }
                        csv.append(val);
                        if (i < rowArray.length - 1) csv.append(",");
                    }
                    csv.append("\n");
                }
            }
            default -> {
                csv.append("Data\n");
                for (Object row : data) {
                    csv.append(row.toString()).append("\n");
                }
            }
        }
        
        return csv.toString();
    }

    /**
     * Find method by name, trying different type signatures for null arguments.
     * Tries: Object.class, String[].class, String.class, Long.class for nulls
     */
    private Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        // Strategy 1: Use actual runtime types
        Class<?>[] types = getArgumentTypes(args);
        try {
            return clazz.getMethod(methodName, types);
        } catch (NoSuchMethodException e) {
            // Fall through to strategy 2
        }

        // Strategy 2: For null args, try String[].class (common for email arrays)
        Class<?>[] types2 = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                types2[i] = String[].class; // Most common nullable type for our use case
            } else {
                types2[i] = args[i].getClass();
            }
        }
        try {
            return clazz.getMethod(methodName, types2);
        } catch (NoSuchMethodException e) {
            // Fall through to strategy 3
        }

        // Strategy 3: For null args, try Object.class
        Class<?>[] types3 = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types3[i] = args[i] != null ? args[i].getClass() : Object.class;
        }
        try {
            return clazz.getMethod(methodName, types3);
        } catch (NoSuchMethodException e) {
            // Strategy 4: Try all parameterized types
        }

        // Strategy 4: Search all methods with matching name and compatible parameter count
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                Class<?>[] paramTypes = m.getParameterTypes();
                boolean compatible = true;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible) return m;
            }
        }

        return null;
    }

    /**
     * Parse argument string into Object array.
     * Supports: strings, numbers, booleans, arrays, null
     */
    private Object[] parseArguments(String argsStr) {
        if (argsStr.isEmpty()) {
            return new Object[0];
        }

        List<Object> args = new ArrayList<>();
        List<String> tokens = tokenizeArgs(argsStr);

        for (String token : tokens) {
            args.add(parseValue(token.trim()));
        }

        return args.toArray(new Object[0]);
    }

    /**
     * Tokenize arguments respecting quotes and brackets
     */
    private List<String> tokenizeArgs(String argsStr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int bracketDepth = 0;

        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);

            if (c == '\'' && !inDoubleQuote) inSingleQuote = !inSingleQuote;
            else if (c == '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote;
            else if ((c == '[' || c == '{') && !inSingleQuote && !inDoubleQuote) bracketDepth++;
            else if ((c == ']' || c == '}') && !inSingleQuote && !inDoubleQuote) bracketDepth--;
            else if (c == ',' && !inSingleQuote && !inDoubleQuote && bracketDepth == 0) {
                tokens.add(current.toString());
                current = new StringBuilder();
                continue;
            }

            current.append(c);
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        return tokens;
    }

    /**
     * Parse a single argument value
     */
    private Object parseValue(String token) {
        token = token.trim();

        if ("null".equals(token)) return null;
        if ("true".equals(token)) return true;
        if ("false".equals(token)) return false;

        // String with quotes
        if ((token.startsWith("'") && token.endsWith("'")) || 
            (token.startsWith("\"") && token.endsWith("\""))) {
            return token.substring(1, token.length() - 1);
        }

        // Array [...]
        if (token.startsWith("[") && token.endsWith("]")) {
            String inner = token.substring(1, token.length() - 1).trim();
            if (inner.isEmpty()) return new String[0];
            List<String> items = tokenizeArgs(inner);
            List<String> result = new ArrayList<>();
            for (String item : items) {
                result.add(parseValue(item.trim()).toString());
            }
            return result.toArray(new String[0]);
        }

        // Numbers
        try {
            if (token.contains(".")) return Double.parseDouble(token);
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            // Fall through to string
        }

        return token;
    }

    /**
     * Get runtime types of arguments for method lookup.
     * For null values, tries to match the method's parameter types.
     */
    private Class<?>[] getArgumentTypes(Object[] args) {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = switch (args[i]) {
                case null -> Object.class; // For null args, use Object.class but prefer String[] for array positions
                case Long l -> Long.class;
                case Integer n -> Integer.class;
                case Double d -> Double.class;
                case Boolean b -> Boolean.class;
                case String[] s -> String[].class;
                case Object obj -> obj.getClass();
            };
        }
        return types;
    }
}
