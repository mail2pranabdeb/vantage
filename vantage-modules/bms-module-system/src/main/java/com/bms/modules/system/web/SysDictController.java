package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysDictType;
import com.pd.modules.system.domain.SysDictData;
import com.pd.modules.system.infrastructure.repository.SysDictTypeRepository;
import com.pd.modules.system.infrastructure.repository.SysDictDataRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/dict")
public class SysDictController extends BaseController {

    private final SysDictTypeRepository dictTypeRepository;
    private final SysDictDataRepository dictDataRepository;

    public SysDictController(SysDictTypeRepository dictTypeRepository, SysDictDataRepository dictDataRepository) {
        this.dictTypeRepository = dictTypeRepository;
        this.dictDataRepository = dictDataRepository;
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/type/list")
    public AjaxResult listType() {
        return success("Dictionary type list retrieved");
    }

    @GetMapping("/data/type/{dictType}")
    public AjaxResult dictType(@PathVariable String dictType) {
        return success("Dictionary data retrieved");
    }

    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/type")
    public AjaxResult addType(@RequestBody SysDictType dict) {
        return success("Dictionary type added");
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/type")
    public AjaxResult editType(@RequestBody SysDictType dict) {
        return success("Dictionary type updated");
    }

    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/type/{dictIds}")
    public AjaxResult removeType(@PathVariable Long[] dictIds) {
        return success("Dictionary type deleted");
    }
}
