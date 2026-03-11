package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysPost;
import com.pd.modules.system.infrastructure.repository.SysPostRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/post")
public class SysPostController extends BaseController {

    private final SysPostRepository postRepository;

    public SysPostController(SysPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @PreAuthorize("hasAuthority('system:post:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(postRepository.findAll());
    }

    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping(value = "/{postId}")
    public AjaxResult getInfo(@PathVariable Long postId) {
        return postRepository.findById(postId)
                .map(this::success)
                .orElse(error("Post not found"));
    }

    @PreAuthorize("hasAuthority('system:post:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SysPost post) {
        postRepository.insert(post);
        return success("Post added");
    }

    @PreAuthorize("hasAuthority('system:post:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysPost post) {
        postRepository.update(post);
        return success("Post updated");
    }

    @PreAuthorize("hasAuthority('system:post:remove')")
    @DeleteMapping("/{postIds}")
    public AjaxResult remove(@PathVariable Long[] postIds) {
        return toAjax(postRepository.deleteByIds(postIds));
    }
}
