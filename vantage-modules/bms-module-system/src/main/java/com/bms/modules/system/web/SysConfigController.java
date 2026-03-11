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
        return success(configRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId) {
        return configRepository.findById(configId)
                .map(this::success)
                .orElse(error("Config not found"));
    }

    @PreAuthorize("hasAuthority('system:config:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysConfig config) {
        config.setConfigType(config.getConfigType() != null ? config.getConfigType() : "Y");
        configRepository.insert(config);
        return success("Config added successfully");
    }

    @PreAuthorize("hasAuthority('system:config:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysConfig config) {
        SysConfig existing = configRepository.findById(config.getConfigId()).orElse(null);
        if (existing == null) {
            return error("Config not found");
        }
        configRepository.update(config);
        return success("Config updated successfully");
    }

    @PreAuthorize("hasAuthority('system:config:remove')")
    @DeleteMapping("/{configId}")
    public AjaxResult remove(@PathVariable Long configId) {
        if (configRepository.findById(configId).isEmpty()) {
            return error("Config not found");
        }
        configRepository.deleteById(configId);
        return success("Config deleted successfully");
    }
}
