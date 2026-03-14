package com.pd.modules.system.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.modules.system.domain.SysOperLog;
import com.pd.modules.system.infrastructure.repository.SysOperLogRepository;

/**
 * Operation log controller
 */
@RestController
@RequestMapping("/system/operlog")
public class SysOperLogController {

    @Autowired
    private SysOperLogRepository operLogRepository;

    /**
     * Get list of operation logs
     */
    @GetMapping("/list")
    public List<SysOperLog> list(String title, String operName, Integer businessType, Integer status) {
        return operLogRepository.findByCondition(title, operName, businessType, status);
    }

    /**
     * Delete operation log by IDs
     */
    @DeleteMapping
    public int remove(@RequestBody Long[] operIds) {
        return operLogRepository.deleteByIds(operIds);
    }

    /**
     * Clean all operation logs
     */
    @DeleteMapping("/clean")
    public void clean() {
        operLogRepository.clean();
    }
}
