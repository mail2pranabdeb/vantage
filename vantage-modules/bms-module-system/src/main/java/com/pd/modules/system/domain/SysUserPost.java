package com.pd.modules.system.domain;

import java.io.Serializable;

/**
 * User-Post relationship entity - sys_user_post
 */
public class SysUserPost implements Serializable {
    private static final long serialVersionUID = 1L;

    /** User ID */
    private Long userId;

    /** Post ID */
    private Long postId;

    public SysUserPost() {
    }

    public SysUserPost(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
