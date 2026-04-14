package com.pd.modules.quartz.util;

import com.pd.modules.quartz.domain.SysJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

        log.info("Sending {} report '{}' to {}", outputFormat, templateName, recipients);
        log.info("Data rows: {}", data.size());

        // Send the report email using SimpleMailMessage (same as test email)
        try {
            Object mailSender = applicationContext.getBean("javaMailSender");
            
            // Use SimpleMailMessage like the working test email code
            Object message = Class.forName("org.springframework.mail.SimpleMailMessage")
                .getDeclaredConstructor()
                .newInstance();

            java.lang.reflect.Method setFromMethod = message.getClass().getMethod("setFrom", String.class);
            setFromMethod.invoke(message, "Vantage Admin <noreply@vantage.com>");

            java.lang.reflect.Method setToMethod = message.getClass().getMethod("setTo", String[].class);
            setToMethod.invoke(message, (Object) recipients.split(","));

            java.lang.reflect.Method setSubjectMethod = message.getClass().getMethod("setSubject", String.class);
            setSubjectMethod.invoke(message, "Report: " + templateName);

            String body = String.format(
                "Automated Report: %s\nGenerated on: %s\nFormat: %s\nTotal rows: %d\n\nThis is an automated report from Vantage Admin.",
                templateName, java.time.LocalDateTime.now(), outputFormat, data.size()
            );
            java.lang.reflect.Method setTextMethod = message.getClass().getMethod("setText", String.class);
            setTextMethod.invoke(message, body);

            // Send the email
            java.lang.reflect.Method sendMethod = mailSender.getClass().getMethod("send", message.getClass());
            sendMethod.invoke(mailSender, message);

            log.info("Report email sent successfully to {}", recipients);
        } catch (Exception e) {
            log.error("Failed to send report email to {}", recipients, e);
        }
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
            if (args[i] == null) {
                // For null args, use Object.class but prefer String[] for array positions
                types[i] = Object.class;
            } else if (args[i] instanceof Long) {
                types[i] = Long.class;
            } else if (args[i] instanceof Integer) {
                types[i] = Integer.class;
            } else if (args[i] instanceof Double) {
                types[i] = Double.class;
            } else if (args[i] instanceof Boolean) {
                types[i] = Boolean.class;
            } else if (args[i] instanceof String[]) {
                types[i] = String[].class;
            } else {
                types[i] = args[i].getClass();
            }
        }
        return types;
    }
}
