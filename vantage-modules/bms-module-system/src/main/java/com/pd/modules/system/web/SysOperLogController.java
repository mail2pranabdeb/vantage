package com.pd.modules.system.web;

import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysOperLog;
import com.pd.modules.system.infrastructure.repository.SysOperLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Operation log controller
 */
@RestController
@RequestMapping("/api/system/operlog")
public class SysOperLogController {

    @Autowired
    private SysOperLogRepository operLogRepository;

    /**
     * Get list of operation logs
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String title,
                          @RequestParam(required = false) String operName,
                          @RequestParam(required = false) Integer businessType,
                          @RequestParam(required = false) Integer status) {
        List<SysOperLog> list = operLogRepository.findByCondition(title, operName, businessType, status);
        return AjaxResult.success(list);
    }

    /**
     * Delete operation log by IDs
     */
    @DeleteMapping
    public AjaxResult remove(@RequestBody Long[] operIds) {
        for (Long id : operIds) {
            operLogRepository.deleteById(id);
        }
        return AjaxResult.success();
    }

    /**
     * Clean all operation logs
     */
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        operLogRepository.deleteAll();
        return AjaxResult.success();
    }
}
