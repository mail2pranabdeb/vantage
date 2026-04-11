package com.pd.modules.quartz.util;

import com.pd.modules.quartz.domain.SysJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        Class<?>[] argTypes = getArgumentTypes(args);

        // Find and invoke method
        Method method = bean.getClass().getMethod(methodName, argTypes);
        if (method == null) {
            throw new IllegalArgumentException("Method not found: " + beanName + "." + methodName);
        }

        log.info("Invoking {}.{} with {} arguments", beanName, methodName, args.length);
        method.invoke(bean, args);
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
     * Get runtime types of arguments for method lookup
     */
    private Class<?>[] getArgumentTypes(Object[] args) {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
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
