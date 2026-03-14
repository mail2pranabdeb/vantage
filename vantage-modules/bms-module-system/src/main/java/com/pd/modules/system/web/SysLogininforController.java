package com.pd.modules.system.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysLogininfor;
import com.pd.modules.system.infrastructure.repository.SysLogininforRepository;

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
    public AjaxResult list(String loginName, String status, String ipaddr) {
        List<SysLogininfor> list = logininforRepository.findByCondition(loginName, status, ipaddr);
        return AjaxResult.success(list);
    }

    /**
     * Delete login info by IDs
     */
    @DeleteMapping
    public AjaxResult remove(@RequestBody Long[] infoIds) {
        int rows = logininforRepository.deleteByIds(infoIds);
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * Clean all login infos
     */
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        logininforRepository.clean();
        return AjaxResult.success();
    }
}
