package com.pd.modules.system.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysOperLog;
import com.pd.modules.system.infrastructure.repository.SysOperLogRepository;

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
    public AjaxResult list(String title, String operName, Integer businessType, Integer status) {
        List<SysOperLog> list = operLogRepository.findByCondition(title, operName, businessType, status);
        return AjaxResult.success(list);
    }

    /**
     * Delete operation log by IDs
     */
    @DeleteMapping
    public AjaxResult remove(@RequestBody Long[] operIds) {
        int rows = operLogRepository.deleteByIds(operIds);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * Clean all operation logs
     */
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        operLogRepository.clean();
        return AjaxResult.success();
    }
}
