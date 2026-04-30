package com.pd.gateway.datasource;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.datasource.domain.SysDatasource;
import com.pd.modules.datasource.service.DatasourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system/datasource")
public class DatasourceController extends BaseController {

    @Autowired
    private DatasourceService datasourceService;

    @PreAuthorize("hasAuthority('system:datasource:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(datasourceService.findAll());
    }

    @PreAuthorize("hasAuthority('system:datasource:query')")
    @GetMapping(value = "/{datasourceId}")
    public AjaxResult getInfo(@PathVariable Long datasourceId) {
        return datasourceService.findById(datasourceId)
            .map(this::success)
            .orElse(error("Datasource not found"));
    }

    @PreAuthorize("hasAuthority('system:datasource:add')")
    @Log(title = "Datasource Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysDatasource datasource) {
        if (datasourceService.existsByDatasourceKey(datasource.getDatasourceKey())) {
            return error("Datasource key already exists");
        }
        
        // Set driver class if not provided
        if (datasource.getDriverClass() == null || datasource.getDriverClass().isEmpty()) {
            datasource.setDriverClass(datasourceService.getDriverClass(datasource.getDbType()));
        }
        
        datasourceService.save(datasource);
        return success("Datasource added successfully");
    }

    @PreAuthorize("hasAuthority('system:datasource:edit')")
    @Log(title = "Datasource Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysDatasource datasource) {
        if (!datasourceService.findById(datasource.getDatasourceId()).isPresent()) {
            return error("Datasource not found");
        }
        
        datasourceService.save(datasource);
        return success("Datasource updated successfully");
    }

    @PreAuthorize("hasAuthority('system:datasource:remove')")
    @Log(title = "Datasource Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{datasourceId}")
    public AjaxResult remove(@PathVariable Long datasourceId) {
        datasourceService.deleteById(datasourceId);
        return success("Datasource deleted successfully");
    }

    @PreAuthorize("hasAuthority('system:datasource:test')")
    @Log(title = "Datasource Management", businessType = BusinessType.OTHER)
    @PostMapping("/test")
    public AjaxResult testConnection(@RequestBody SysDatasource datasource) {
        try {
            boolean success = datasourceService.testConnection(datasource);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("message", success ? "Connection successful!" : "Connection failed");
            result.put("lastTestTime", datasource.getLastTestTime());
            result.put("lastTestStatus", datasource.getLastTestStatus());
            
            return success(result);
        } catch (Exception e) {
            return error("Connection test failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('system:datasource:query')")
    @GetMapping("/driver/{dbType}")
    public AjaxResult getDriverClass(@PathVariable String dbType) {
        Map<String, String> result = new HashMap<>();
        result.put("driverClass", datasourceService.getDriverClass(dbType));
        result.put("urlPattern", datasourceService.getDefaultUrlPattern(dbType));
        return success(result);
    }
}
