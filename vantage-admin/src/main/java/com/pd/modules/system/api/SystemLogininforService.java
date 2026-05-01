package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.LogininforDTO;
import java.util.List;
import java.util.Optional;

/**
 * System module public API for login info operations.
 */
public interface SystemLogininforService {

    List<LogininforDTO> findAll();

    List<LogininforDTO> findRecent(int limit);

    List<LogininforDTO> findByLoginName(String loginName);

    List<LogininforDTO> findByCondition(String loginName, String status, String ipaddr);

    List<LogininforDTO> findFailedAttempts(int limit);

    Optional<LogininforDTO> findById(Long infoId);

    boolean deleteByIds(Long[] infoIds);

    void cleanLogs();

    void recordLogininfor(LogininforDTO logininfor);

    long count();

    long countByStatus(String status);
}
