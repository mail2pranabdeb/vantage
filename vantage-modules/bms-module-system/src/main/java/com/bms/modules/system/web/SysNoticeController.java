package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysNotice;
import com.pd.modules.system.infrastructure.repository.SysNoticeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
        return success(noticeRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Integer noticeId) {
        return noticeRepository.findById(noticeId)
                .map(this::success)
                .orElse(error("Notice not found"));
    }

    @PreAuthorize("hasAuthority('system:notice:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysNotice notice) {
        notice.setStatus(notice.getStatus() != null ? notice.getStatus() : "0");
        noticeRepository.insert(notice);
        return success("Notice added successfully");
    }

    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysNotice notice) {
        SysNotice existing = noticeRepository.findById(notice.getNoticeId()).orElse(null);
        if (existing == null) {
            return error("Notice not found");
        }
        noticeRepository.update(notice);
        return success("Notice updated successfully");
    }

    @PreAuthorize("hasAuthority('system:notice:remove')")
    @DeleteMapping("/{noticeId}")
    public AjaxResult remove(@PathVariable Integer noticeId) {
        if (noticeRepository.findById(noticeId).isEmpty()) {
            return error("Notice not found");
        }
        noticeRepository.deleteById(noticeId);
        return success("Notice deleted successfully");
    }
}
