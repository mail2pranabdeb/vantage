package com.pd.modules.system.web;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.system.domain.SysPost;
import com.pd.modules.system.infrastructure.repository.SysPostRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/system/post")
public class SysPostController extends BaseController {

    private final SysPostRepository postRepository;

    public SysPostController(SysPostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @PreAuthorize("hasAuthority('system:post:list')")
    @GetMapping("/list")
    public AjaxResult list() {
        return success(postRepository.findAllActive());
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
        post.setPostSort(post.getPostSort() != null ? post.getPostSort() : 0);
        post.setStatus(post.getStatus() != null ? post.getStatus() : "0");
        post.setCreateBy("admin");
        post.setCreateTime(LocalDateTime.now());
        postRepository.save(post);
        return success("Post added successfully");
    }

    @PreAuthorize("hasAuthority('system:post:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SysPost post) {
        Optional<SysPost> existing = postRepository.findById(post.getPostId());
        if (!existing.isPresent()) {
            return error("Post not found");
        }
        post.setUpdateBy("admin");
        post.setUpdateTime(LocalDateTime.now());
        postRepository.save(post);
        return success("Post updated successfully");
    }

    @PreAuthorize("hasAuthority('system:post:remove')")
    @DeleteMapping("/{postId}")
    public AjaxResult remove(@PathVariable Long postId) {
        if (!postRepository.findById(postId).isPresent()) {
            return error("Post not found");
        }
        postRepository.deleteById(postId);
        return success("Post deleted successfully");
    }
}
