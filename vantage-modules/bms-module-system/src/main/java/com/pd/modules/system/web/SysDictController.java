package com.pd.modules.system.web;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysDictData;
import com.pd.modules.system.domain.SysDictType;
import com.pd.modules.system.infrastructure.repository.SysDictDataRepository;
import com.pd.modules.system.infrastructure.repository.SysDictTypeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/dict")
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
        return success(dictTypeRepository.findAllActive());
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/data/list")
    public AjaxResult listData(@RequestParam String dictType) {
        List<SysDictData> dictData = dictDataRepository.findByDictTypeOrderBySort(dictType);
        return success(dictData);
    }

    @PreAuthorize("hasAuthority('system:dict:list')")
    @GetMapping("/data/type/{dictType}")
    public AjaxResult getDataByType(@PathVariable String dictType) {
        List<SysDictData> dictData = dictDataRepository.findByDictTypeOrderBySort(dictType);
        return success(dictData);
    }

    @GetMapping("/type/get-by-code/{dictType}")
    public AjaxResult getTypeByCode(@PathVariable String dictType) {
        Optional<SysDictType> type = dictTypeRepository.findByDictType(dictType);
        return type.map(this::success).orElse(success(null));
    }

    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/type/{dictId}")
    public AjaxResult getType(@PathVariable Long dictId) {
        Optional<SysDictType> dictType = dictTypeRepository.findById(dictId);
        return dictType.map(this::success).orElse(error("Dictionary type not found"));
    }

    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "Dictionary Management", businessType = BusinessType.INSERT)
    @PostMapping("/type")
    public AjaxResult addType(@RequestBody SysDictType dict) {
        dict.setStatus(dict.getStatus() != null ? dict.getStatus() : "0");
        dict.setCreateBy("admin");
        dict.setCreateTime(LocalDateTime.now());
        dictTypeRepository.save(dict);
        return success("Dictionary type added successfully");
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "Dictionary Management", businessType = BusinessType.UPDATE)
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
    @Log(title = "Dictionary Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/type/{dictId}")
    public AjaxResult removeType(@PathVariable Long dictId) {
        if (!dictTypeRepository.findById(dictId).isPresent()) {
            return error("Dictionary type not found");
        }
        dictTypeRepository.deleteById(dictId);
        return success("Dictionary type deleted successfully");
    }

    @PreAuthorize("hasAuthority('system:dict:add')")
    @Log(title = "Dictionary Data", businessType = BusinessType.INSERT)
    @PostMapping("/data")
    public AjaxResult addData(@RequestBody SysDictData dictData) {
        dictData.setStatus(dictData.getStatus() != null ? dictData.getStatus() : "0");
        dictData.setCreateBy("admin");
        dictData.setCreateTime(LocalDateTime.now());
        dictDataRepository.save(dictData);
        return success("Dictionary data added successfully");
    }

    @PreAuthorize("hasAuthority('system:dict:edit')")
    @Log(title = "Dictionary Data", businessType = BusinessType.UPDATE)
    @PutMapping("/data")
    public AjaxResult editData(@RequestBody SysDictData dictData) {
        if (dictData.getDictCode() == null || !dictDataRepository.findById(dictData.getDictCode()).isPresent()) {
            return error("Dictionary data not found");
        }
        dictData.setUpdateBy("admin");
        dictData.setUpdateTime(LocalDateTime.now());
        dictDataRepository.save(dictData);
        return success("Dictionary data updated successfully");
    }

    @PreAuthorize("hasAuthority('system:dict:remove')")
    @Log(title = "Dictionary Data", businessType = BusinessType.DELETE)
    @DeleteMapping("/data/{dictCode}")
    public AjaxResult removeData(@PathVariable Long dictCode) {
        if (!dictDataRepository.findById(dictCode).isPresent()) {
            return error("Dictionary data not found");
        }
        dictDataRepository.deleteById(dictCode);
        return success("Dictionary data deleted successfully");
    }
}
