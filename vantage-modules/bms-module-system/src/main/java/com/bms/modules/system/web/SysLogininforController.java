package com.pd.modules.system.web;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.pd.modules.system.domain.SysLogininfor;
import com.pd.modules.system.infrastructure.repository.SysLogininforRepository;

/**
 * Login info controller
 */
@RestController
@RequestMapping("/system/logininfor")
public class SysLogininforController {

    @Autowired
    private SysLogininforRepository logininforRepository;

    /**
     * Get list of login infos
     */
    @GetMapping("/list")
    public List<SysLogininfor> list(String loginName, String status, String ipaddr) {
        return logininforRepository.findByCondition(loginName, status, ipaddr);
    }

    /**
     * Delete login info by IDs
     */
    @DeleteMapping
    public int remove(@RequestBody Long[] infoIds) {
        return logininforRepository.deleteByIds(infoIds);
    }

    /**
     * Clean all login infos
     */
    @DeleteMapping("/clean")
    public void clean() {
        logininforRepository.clean();
    }
}
