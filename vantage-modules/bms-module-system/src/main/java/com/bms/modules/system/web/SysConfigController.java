package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysConfig;
import com.pd.modules.system.infrastructure.repository.SysConfigRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/config")
public class SysConfigController extends BaseController {

    private final SysConfigRepository configRepository;

    public SysConfigController(SysConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @PreAuthorize("hasAuthority('system:config:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success("Configuration list retrieved");
    }

    @GetMapping(value = "/configKey/{configKey}")
    public AjaxResult getConfigKey(@PathVariable String configKey) {
        return success("Configuration key retrieved");
    }

    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId) {
        return success("Configuration info retrieved");
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysConfig config) {
        return success("Configuration added");
    }

    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysConfig config) {
        return success("Configuration updated");
    }

    @PreAuthorize("hasAuthority('system:config:remove')")
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds) {
        return success("Configuration deleted");
    }
}
