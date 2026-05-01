package com.pd.modules.quartz.service.impl;

import com.pd.modules.quartz.api.QuartzScriptJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.StringWriter;
import java.util.*;

@Service
public class QuartzScriptJobServiceImpl implements QuartzScriptJobService {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, String>> runScript(String scriptType, String scriptContent) {
        List<Map<String, String>> output = new ArrayList<>();

        if ("sql".equalsIgnoreCase(scriptType)) {
            return executeSqlScript(scriptContent, output);
        }

        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = getEngineByType(manager, scriptType);

        if (engine == null) {
            throw new RuntimeException("Script engine not found for type: " + scriptType + ". Please ensure the JSR-223 engine is installed.");
        }

        StringWriter writer = new StringWriter();
        engine.getContext().setWriter(writer);
        engine.put("output", output);
        engine.put("log", new ScriptLogger(output));
        try {
            engine.eval(scriptContent);
        } catch (ScriptException e) {
            throw new RuntimeException("Script execution failed: " + e.getMessage(), e);
        }

        String capturedOutput = writer.toString();
        if (!capturedOutput.isEmpty()) {
            output.add(Map.of("type", "output", "message", capturedOutput.trim()));
        }

        return output;
    }

    private List<Map<String, String>> executeSqlScript(String sql, List<Map<String, String>> output) {
        if (jdbcTemplate == null) {
            throw new RuntimeException("Database connection not configured");
        }
        output.add(Map.of("type", "info", "message", "Executing SQL script..."));
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        output.add(Map.of("type", "success", "message", "Query executed successfully!"));
        output.add(Map.of("type", "info", "message", "Rows returned: " + results.size()));
        int displayRows = Math.min(results.size(), 10);
        for (int i = 0; i < displayRows; i++) {
            output.add(Map.of("type", "output", "message", "Row " + (i + 1) + ": " + results.get(i).toString()));
        }
        if (results.size() > 10) {
            output.add(Map.of("type", "info", "message", "... and " + (results.size() - 10) + " more rows"));
        }
        return output;
    }

    private ScriptEngine getEngineByType(ScriptEngineManager manager, String scriptType) {
        return switch (scriptType.toLowerCase()) {
            case "javascript", "js" -> manager.getEngineByName("JavaScript");
            case "groovy" -> manager.getEngineByName("groovy");
            case "python" -> manager.getEngineByName("python");
            default -> null;
        };
    }

    public static class ScriptLogger {
        private final List<Map<String, String>> output;

        public ScriptLogger(List<Map<String, String>> output) {
            this.output = output;
        }

        public void info(String message) {
            output.add(Map.of("type", "info", "message", message));
        }

        public void warn(String message) {
            output.add(Map.of("type", "warn", "message", message));
        }

        public void error(String message) {
            output.add(Map.of("type", "error", "message", message));
        }

        public void debug(String message) {
            output.add(Map.of("type", "debug", "message", message));
        }
    }
}
