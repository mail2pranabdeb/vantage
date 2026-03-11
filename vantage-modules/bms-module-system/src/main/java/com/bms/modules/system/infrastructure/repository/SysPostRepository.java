package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysPost;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SysPostRepository {
    private final JdbcTemplate jdbcTemplate;

    public SysPostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysPost> rowMapper = (rs, rowNum) -> {
        SysPost post = new SysPost();
        post.setPostId(rs.getLong("post_id"));
        post.setPostCode(rs.getString("post_code"));
        post.setPostName(rs.getString("post_name"));
        post.setPostSort(rs.getInt("post_sort"));
        post.setStatus(rs.getString("status"));
        return post;
    };

    public List<SysPost> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_post ORDER BY post_sort", rowMapper);
    }

    public Optional<SysPost> findById(Long postId) {
        List<SysPost> posts = jdbcTemplate.query(
                "SELECT * FROM sys_post WHERE post_id = ?",
                rowMapper,
                postId);
        return posts.stream().findFirst();
    }

    public int insert(SysPost post) {
        String sql = "INSERT INTO sys_post (post_code, post_name, post_sort, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, current_timestamp)";
        return jdbcTemplate.update(sql,
                post.getPostCode(),
                post.getPostName(),
                post.getPostSort(),
                post.getStatus(),
                "admin");
    }

    public int update(SysPost post) {
        String sql = "UPDATE sys_post SET post_code = ?, post_name = ?, post_sort = ?, status = ?, update_by = ?, update_time = current_timestamp WHERE post_id = ?";
        return jdbcTemplate.update(sql,
                post.getPostCode(),
                post.getPostName(),
                post.getPostSort(),
                post.getStatus(),
                "admin",
                post.getPostId());
    }

    public int deleteById(Long postId) {
        return jdbcTemplate.update("DELETE FROM sys_post WHERE post_id = ?", postId);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM sys_post WHERE post_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }
}
