package com.pd.modules.quartz.infrastructure.repository;

import com.pd.modules.quartz.domain.SysJobLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class SysJobLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysJobLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysJobLog> rowMapper = (rs, rowNum) -> {
        SysJobLog log = new SysJobLog();
        log.setJobLogId(rs.getLong("job_log_id"));
        log.setJobName(rs.getString("job_name"));
        log.setJobGroup(rs.getString("job_group"));
        log.setInvokeTarget(rs.getString("invoke_target"));
        log.setJobMessage(rs.getString("job_message"));
        log.setStatus(rs.getString("status"));
        log.setExceptionInfo(rs.getString("exception_info"));
        java.sql.Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) {
            log.setStartTime(startTime.toLocalDateTime());
        }
        java.sql.Timestamp endTime = rs.getTimestamp("end_time");
        if (endTime != null) {
            log.setEndTime(endTime.toLocalDateTime());
        }
        return log;
    };

    public List<SysJobLog> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_job_log ORDER BY start_time DESC", rowMapper);
    }

    public List<SysJobLog> findByCondition(String jobName, String jobGroup, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_job_log WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();
        
        if (jobName != null && !jobName.isEmpty()) {
            sql.append(" AND job_name LIKE ?");
            params.add("%" + jobName + "%");
        }
        if (jobGroup != null && !jobGroup.isEmpty()) {
            sql.append(" AND job_group = ?");
            params.add(jobGroup);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        sql.append(" ORDER BY start_time DESC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<SysJobLog> findById(Long jobLogId) {
        List<SysJobLog> logs = jdbcTemplate.query(
                "SELECT * FROM sys_job_log WHERE job_log_id = ?",
                rowMapper,
                jobLogId);
        return logs.stream().findFirst();
    }

    public int insert(SysJobLog log) {
        String sql = "INSERT INTO sys_job_log (job_name, job_group, invoke_target, job_message, status, exception_info, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                log.getJobName(),
                log.getJobGroup(),
                log.getInvokeTarget(),
                log.getJobMessage(),
                log.getStatus(),
                log.getExceptionInfo(),
                log.getStartTime() != null ? Timestamp.valueOf(log.getStartTime()) : null,
                log.getEndTime() != null ? Timestamp.valueOf(log.getEndTime()) : null);
    }

    public int deleteById(Long jobLogId) {
        return jdbcTemplate.update("DELETE FROM sys_job_log WHERE job_log_id = ?", jobLogId);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM sys_job_log WHERE job_log_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }

    public int clean() {
        return jdbcTemplate.update("TRUNCATE TABLE sys_job_log");
    }
}
