package com.pd.modules.quartz.infrastructure.repository;

import com.pd.modules.quartz.domain.SysJob;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class SysJobRepository {

    private final JdbcTemplate jdbcTemplate;

    public SysJobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysJob> rowMapper = (rs, rowNum) -> {
        SysJob job = new SysJob();
        job.setJobId(rs.getLong("job_id"));
        job.setJobName(rs.getString("job_name"));
        job.setJobGroup(rs.getString("job_group"));
        job.setInvokeTarget(rs.getString("invoke_target"));
        job.setCronExpression(rs.getString("cron_expression"));
        job.setMisfirePolicy(rs.getString("misfire_policy"));
        job.setConcurrent(rs.getString("concurrent"));
        job.setStatus(rs.getString("status"));
        job.setCreateBy(rs.getString("create_by"));
        java.sql.Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) {
            job.setCreateTime(createTime.toLocalDateTime());
        }
        job.setUpdateBy(rs.getString("update_by"));
        java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
        if (updateTime != null) {
            job.setUpdateTime(updateTime.toLocalDateTime());
        }
        job.setRemark(rs.getString("remark"));
        return job;
    };

    public List<SysJob> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_job", rowMapper);
    }

    public List<SysJob> findByCondition(String jobName, String jobGroup, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sys_job WHERE 1=1");
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
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Optional<SysJob> findById(Long jobId) {
        List<SysJob> jobs = jdbcTemplate.query(
                "SELECT * FROM sys_job WHERE job_id = ?",
                rowMapper,
                jobId);
        return jobs.stream().findFirst();
    }

    public int insert(SysJob job) {
        String sql = "INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                job.getJobName(),
                job.getJobGroup(),
                job.getInvokeTarget(),
                job.getCronExpression(),
                job.getMisfirePolicy(),
                job.getConcurrent(),
                job.getStatus(),
                job.getCreateBy(),
                Timestamp.valueOf(job.getCreateTime()),
                job.getRemark());
    }

    public int update(SysJob job) {
        String sql = "UPDATE sys_job SET job_name = ?, job_group = ?, invoke_target = ?, cron_expression = ?, misfire_policy = ?, concurrent = ?, status = ?, update_by = ?, update_time = ?, remark = ? WHERE job_id = ?";
        return jdbcTemplate.update(sql,
                job.getJobName(),
                job.getJobGroup(),
                job.getInvokeTarget(),
                job.getCronExpression(),
                job.getMisfirePolicy(),
                job.getConcurrent(),
                job.getStatus(),
                job.getUpdateBy(),
                job.getUpdateTime() != null ? Timestamp.valueOf(job.getUpdateTime()) : null,
                job.getRemark(),
                job.getJobId());
    }

    public int deleteById(Long jobId) {
        return jdbcTemplate.update("DELETE FROM sys_job WHERE job_id = ?", jobId);
    }

    public int deleteByIds(Long[] ids) {
        return jdbcTemplate.batchUpdate(
                "DELETE FROM sys_job WHERE job_id = ?",
                java.util.Arrays.stream(ids)
                        .map(id -> new Object[]{id})
                        .toList())
                .length;
    }
}
