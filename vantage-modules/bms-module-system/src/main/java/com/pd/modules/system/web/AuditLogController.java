package com.pd.modules.system.web;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysAuditLog;
import com.pd.modules.system.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Audit Log Controller
 */
@RestController
@RequestMapping("/api/system/audit")
public class AuditLogController extends BaseController {

    @Autowired
    private AuditLogService auditLogService;

    /**
     * Get audit logs with pagination
     */
    @PreAuthorize("hasAuthority('system:audit:list')")
    @GetMapping("/list")
    public AjaxResult list(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by("operationTime").descending());
        
        Page<SysAuditLog> page;
        if (tableName != null || operator != null || module != null || startTime != null || endTime != null) {
            page = auditLogService.searchAuditLogs(tableName, operator, module, startTime, endTime, pageable);
        } else {
            page = auditLogService.getAuditLogs(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("rows", page.getContent());
        result.put("total", page.getTotalElements());
        
        return success(result);
    }

    /**
     * Get audit history for specific record
     */
    @PreAuthorize("hasAuthority('system:audit:query')")
    @GetMapping("/history/{tableName}/{recordId}")
    public AjaxResult history(@PathVariable String tableName, @PathVariable Long recordId) {
        List<SysAuditLog> logs = auditLogService.getAuditHistory(tableName, recordId);
        return success(logs);
    }

    /**
     * Get audit statistics
     */
    @PreAuthorize("hasAuthority('system:audit:query')")
    @GetMapping("/statistics")
    public AjaxResult statistics() {
        Map<String, Object> stats = auditLogService.getAuditStatistics();
        return success(stats);
    }

    /**
     * Get audit log details
     */
    @PreAuthorize("hasAuthority('system:audit:query')")
    @GetMapping("/{auditId}")
    public AjaxResult getInfo(@PathVariable Long auditId) {
        // Implementation to get single audit log
        return success();
    }
}
