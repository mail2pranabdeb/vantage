package com.pd.gateway.generator;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.generator.service.GenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tool/gen")
public class GenController extends BaseController {

    @Autowired
    private GenService genService;

    @PreAuthorize("hasAuthority('tool:gen:list')")
    @GetMapping("/db/tables")
    public AjaxResult listTables() {
        try {
            List<Map<String, Object>> tables = genService.getDatabaseTables();
            return success(tables);
        } catch (Exception e) {
            return error("Failed to load tables: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('tool:gen:preview')")
    @GetMapping("/preview")
    public void preview(@RequestParam String table, HttpServletResponse response) throws IOException {
        String code = genService.generateCode(table);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(code);
    }

    @PreAuthorize("hasAuthority('tool:gen:code')")
    @Log(title = "Code Generation", businessType = BusinessType.GEN)
    @PostMapping("/batch")
    public AjaxResult batchGen(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<String> tables = (List<String>) params.get("tables");
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) params.get("config");
            
            genService.batchGenerate(tables, config);
            return success("Code generated successfully");
        } catch (Exception e) {
            return error("Failed to generate code: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('tool:gen:download')")
    @Log(title = "Code Generation", businessType = BusinessType.EXPORT)
    @GetMapping("/download")
    public void download(@RequestParam String tables, HttpServletResponse response) throws IOException {
        try {
            String[] tableArray = tables.split(",");
            genService.downloadCode(tableArray, response);
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}
