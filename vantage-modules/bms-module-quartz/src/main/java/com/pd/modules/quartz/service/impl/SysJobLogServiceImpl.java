package com.pd.modules.quartz.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pd.modules.quartz.domain.SysJobLog;
import com.pd.modules.quartz.infrastructure.repository.SysJobLogRepository;
import com.pd.modules.quartz.service.ISysJobLogService;

/**
 * Scheduled job log service implementation
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService {

    @Autowired
    private SysJobLogRepository jobLogRepository;

    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return jobLogRepository.findByCondition(jobLog.getJobName(), jobLog.getJobGroup(), jobLog.getStatus());
    }

    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return jobLogRepository.findById(jobLogId).orElse(null);
    }

    @Override
    public void addJobLog(SysJobLog jobLog) {
        jobLogRepository.insert(jobLog);
    }

    @Override
    public int deleteJobLogByIds(Long[] ids) {
        return jobLogRepository.deleteByIds(ids);
    }

    @Override
    public int deleteJobLogById(Long jobLogId) {
        return jobLogRepository.deleteById(jobLogId);
    }

    @Override
    public void cleanJobLog() {
        jobLogRepository.clean();
    }
}
