package com.pd.modules.job.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.*;

import javax.script.*;
import java.io.StringWriter;
import java.util.*;

@RestController
@RequestMapping("/api/system/scriptJob")
public class ScriptJobController extends BaseController {

    /**
     * Execute script immediately
     */
    @PostMapping("/run")
    public AjaxResult runScript(@RequestBody Map<String, String> params) {
        try {
            String scriptType = params.get("scriptType");
            String scriptContent = params.get("scriptContent");
            
            if (scriptContent == null || scriptContent.isEmpty()) {
                return error("Script content is required");
            }
            
            List<Map<String, String>> output = new ArrayList<>();
            
            // Create script engine
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = getEngineByType(manager, scriptType);
            
            if (engine == null) {
                return error("Script engine not found for type: " + scriptType);
            }
            
            // Capture output
            StringWriter writer = new StringWriter();
            engine.getContext().setWriter(writer);
            
            // Add bindings
            engine.put("output", output);
            engine.put("log", new ScriptLogger(output));
            
            // Execute script
            engine.eval(scriptContent);
            
            // Get captured output
            String capturedOutput = writer.toString();
            if (!capturedOutput.isEmpty()) {
                output.add(Map.of("type", "output", "message", capturedOutput.trim()));
            }
            
            return success(output);
        } catch (Exception e) {
            return error("Script execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Get script engine by type
     */
    private ScriptEngine getEngineByType(ScriptEngineManager manager, String scriptType) {
        switch (scriptType.toLowerCase()) {
            case "javascript":
            case "js":
                return manager.getEngineByName("JavaScript");
            case "groovy":
                return manager.getEngineByName("groovy");
            case "python":
                return manager.getEngineByName("python");
            default:
                return manager.getEngineByName("JavaScript");
        }
    }
    
    /**
     * Logger for scripts
     */
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
