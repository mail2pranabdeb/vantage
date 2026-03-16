package com.pd.modules.system.web;

import com.pd.common.annotation.Log;
import com.pd.common.annotation.Log.BusinessType;
import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysNotice;
import com.pd.modules.system.infrastructure.repository.SysNoticeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/notice")
public class SysNoticeController extends BaseController {

    private final SysNoticeRepository noticeRepository;

    public SysNoticeController(SysNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @PreAuthorize("hasAuthority('system:notice:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(noticeRepository.findAllActive());
    }

    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Integer noticeId) {
        return noticeRepository.findById(noticeId)
                .map(this::success)
                .orElse(error("Notice not found"));
    }

    @PreAuthorize("hasAuthority('system:notice:add')")
    @Log(title = "Notice Management", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysNotice notice) {
        notice.setStatus(notice.getStatus() != null ? notice.getStatus() : "0");
        notice.setCreateBy("admin");
        notice.setCreateTime(LocalDateTime.now());
        noticeRepository.save(notice);
        return success("Notice added successfully");
    }

    @PreAuthorize("hasAuthority('system:notice:edit')")
    @Log(title = "Notice Management", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysNotice notice) {
        Optional<SysNotice> existing = noticeRepository.findById(notice.getNoticeId());
        if (!existing.isPresent()) {
            return error("Notice not found");
        }
        notice.setUpdateBy("admin");
        notice.setUpdateTime(LocalDateTime.now());
        noticeRepository.save(notice);
        return success("Notice updated successfully");
    }

    @PreAuthorize("hasAuthority('system:notice:remove')")
    @Log(title = "Notice Management", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeId}")
    public AjaxResult remove(@PathVariable Integer noticeId) {
        if (!noticeRepository.findById(noticeId).isPresent()) {
            return error("Notice not found");
        }
        noticeRepository.deleteById(noticeId);
        return success("Notice deleted successfully");
    }
}
