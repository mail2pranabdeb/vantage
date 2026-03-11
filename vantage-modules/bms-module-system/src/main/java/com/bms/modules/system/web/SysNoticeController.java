package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysNotice;
import com.pd.modules.system.infrastructure.repository.SysNoticeRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {

    private final SysNoticeRepository noticeRepository;

    public SysNoticeController(SysNoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @PreAuthorize("hasAuthority('system:notice:list')")
    @GetMapping("/list")
    public AjaxResult list(SysNotice notice) {
        return success(noticeRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping(value = "/{noticeId}")
    public AjaxResult getInfo(@PathVariable Long noticeId) {
        return noticeRepository.findById(noticeId)
                .map(this::success)
                .orElse(error("Notice not found"));
    }

    @PreAuthorize("hasAuthority('system:notice:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysNotice notice) {
        noticeRepository.insert(notice);
        return success("Notice added");
    }

    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysNotice notice) {
        noticeRepository.update(notice);
        return success("Notice updated");
    }

    @PreAuthorize("hasAuthority('system:notice:remove')")
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeRepository.deleteByIds(noticeIds));
    }
}
