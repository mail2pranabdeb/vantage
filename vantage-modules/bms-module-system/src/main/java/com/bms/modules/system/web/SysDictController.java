package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysDictType;
import com.pd.modules.system.infrastructure.repository.SysDictTypeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/dict")
public class SysDictController extends BaseController {

    private final SysDictTypeRepository dictTypeRepository;

    public SysDictController(SysDictTypeRepository dictTypeRepository) {
        this.dictTypeRepository = dictTypeRepository;
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/type/list")
    public AjaxResult listType() {
        return success(dictTypeRepository.findAllActive());
    }

    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/type/{dictId}")
    public AjaxResult getType(@PathVariable Long dictId) {
        Optional<SysDictType> dictType = dictTypeRepository.findById(dictId);
        return dictType.map(this::success).orElse(error("Dictionary type not found"));
    }

    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/type")
    public AjaxResult addType(@RequestBody SysDictType dict) {
        dict.setStatus(dict.getStatus() != null ? dict.getStatus() : "0");
        dict.setCreateBy("admin");
        dict.setCreateTime(LocalDateTime.now());
        dictTypeRepository.save(dict);
        return success("Dictionary type added successfully");
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/type")
    public AjaxResult editType(@RequestBody SysDictType dict) {
        Optional<SysDictType> existing = dictTypeRepository.findById(dict.getDictId());
        if (!existing.isPresent()) {
            return error("Dictionary type not found");
        }
        dict.setUpdateBy("admin");
        dict.setUpdateTime(LocalDateTime.now());
        dictTypeRepository.save(dict);
        return success("Dictionary type updated successfully");
    }

    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/type/{dictId}")
    public AjaxResult removeType(@PathVariable Long dictId) {
        if (!dictTypeRepository.findById(dictId).isPresent()) {
            return error("Dictionary type not found");
        }
        dictTypeRepository.deleteById(dictId);
        return success("Dictionary type deleted successfully");
    }
}
