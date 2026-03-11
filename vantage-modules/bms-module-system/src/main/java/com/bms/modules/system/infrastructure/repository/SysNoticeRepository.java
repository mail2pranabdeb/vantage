package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysNotice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SysNoticeRepository {
    private final JdbcTemplate jdbcTemplate;

    public SysNoticeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysNotice> rowMapper = (rs, rowNum) -> {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(rs.getInt("notice_id"));
        notice.setNoticeTitle(rs.getString("notice_title"));
        notice.setNoticeType(rs.getString("notice_type"));
        notice.setNoticeContent(rs.getBytes("notice_content"));
        notice.setStatus(rs.getString("status"));
        return notice;
    };

    public List<SysNotice> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_notice ORDER BY create_time DESC", rowMapper);
    }

    public Optional<SysNotice> findById(Long noticeId) {
        List<SysNotice> notices = jdbcTemplate.query(
                "SELECT * FROM sys_notice WHERE notice_id = ?",
                rowMapper,
                noticeId);
        return notices.stream().findFirst();
    }

    public int insert(SysNotice notice) {
        String sql = "INSERT INTO sys_notice (notice_title, notice_type, notice_content, status, create_by, create_time) VALUES (?, ?, ?, ?, ?, current_timestamp)";
        return jdbcTemplate.update(sql,
                notice.getNoticeTitle(),
                notice.getNoticeType(),
                notice.getNoticeContent(),
                notice.getStatus());
    }

    public int update(SysNotice notice) {
        String sql = "UPDATE sys_notice SET notice_title = ?, notice_type = ?, notice_content = ?, status = ?, update_by = ?, update_time = current_timestamp WHERE notice_id = ?";
        return jdbcTemplate.update(sql,
                notice.getNoticeTitle(),
                notice.getNoticeType(),
                notice.getNoticeContent(),
                notice.getStatus(),
                "admin",
                notice.getNoticeId());
    }

    public int deleteById(Long noticeId) {
        return jdbcTemplate.update("DELETE FROM sys_notice WHERE notice_id = ?", noticeId);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM sys_notice WHERE notice_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }
}
