package com.pd.modules.quartz.service.impl;

import java.util.List;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.infrastructure.repository.SysJobRepository;
import com.pd.modules.quartz.service.ISysJobService;
import com.pd.modules.quartz.util.CronUtils;
import com.pd.modules.quartz.util.ScheduleUtils;

/**
 * Scheduled job service implementation
 */
@Service
public class SysJobServiceImpl implements ISysJobService {

    @Autowired
    private SysJobRepository jobRepository;

    @Autowired
    private Scheduler scheduler;

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return jobRepository.findByCondition(job.getJobName(), job.getJobGroup(), job.getStatus());
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return jobRepository.findById(jobId).orElse(null);
    }

    @Override
    public int pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus("1");
        int rows = jobRepository.update(job);
        if (rows > 0) {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    @Override
    public int resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus("0");
        int rows = jobRepository.update(job);
        if (rows > 0) {
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    @Override
    public int deleteJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        int rows = jobRepository.deleteById(jobId);
        if (rows > 0) {
            scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    @Override
    public void deleteJobByIds(Long[] ids) throws SchedulerException {
        for (Long jobId : ids) {
            SysJob job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                deleteJob(job);
            }
        }
    }

    @Override
    public int changeStatus(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        String status = job.getStatus();
        
        if ("0".equals(status)) {
            resumeJob(job);
        } else if ("1".equals(status)) {
            pauseJob(job);
        }
        return jobRepository.update(job);
    }

    @Override
    public boolean run(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        SysJob sysJob = selectJobById(jobId);
        if (sysJob == null) {
            return false;
        }
        scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        return true;
    }

    @Override
    public int insertJob(SysJob job) throws SchedulerException {
        int rows = jobRepository.insert(job);
        if (rows > 0) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return rows;
    }

    @Override
    public int updateJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        ScheduleUtils.createScheduleJob(scheduler, job);
        return jobRepository.update(job);
    }

    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }

    private Long[] parseIds(String ids) {
        if (ids == null || ids.isEmpty()) {
            return new Long[0];
        }
        String[] idArr = ids.split(",");
        Long[] result = new Long[idArr.length];
        for (int i = 0; i < idArr.length; i++) {
            result[i] = Long.parseLong(idArr[i]);
        }
        return result;
    }
}
