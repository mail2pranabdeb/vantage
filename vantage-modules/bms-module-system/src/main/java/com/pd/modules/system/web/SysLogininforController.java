package com.pd.modules.system.web;

import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysLogininfor;
import com.pd.modules.system.infrastructure.repository.SysLogininforRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Login info controller
 */
@RestController
@RequestMapping("/api/system/logininfor")
public class SysLogininforController {

    @Autowired
    private SysLogininforRepository logininforRepository;

    /**
     * Get list of login infos
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String loginName,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String ipaddr) {
        List<SysLogininfor> list = logininforRepository.findByCondition(loginName, status, ipaddr);
        return AjaxResult.success(list);
    }

    /**
     * Delete login info by IDs
     */
    @DeleteMapping
    public AjaxResult remove(@RequestBody Long[] infoIds) {
        for (Long id : infoIds) {
            logininforRepository.deleteById(id);
        }
        return AjaxResult.success();
    }

    /**
     * Clean all login infos
     */
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        logininforRepository.deleteAll();
        return AjaxResult.success();
    }
}
