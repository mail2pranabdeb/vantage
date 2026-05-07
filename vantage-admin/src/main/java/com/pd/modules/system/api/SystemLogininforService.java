package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.LogininforDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SystemLogininforService {

    List<LogininforDTO> findAll();

    List<LogininforDTO> findRecent(int limit);

    List<LogininforDTO> findByLoginName(String loginName);

    List<LogininforDTO> findByCondition(String loginName, String status, String ipaddr);

    Page<LogininforDTO> findByConditionPaginated(String loginName, String status, String ipaddr, Pageable pageable);

    List<LogininforDTO> findFailedAttempts(int limit);

    Optional<LogininforDTO> findById(Long infoId);

    boolean deleteByIds(Long[] infoIds);

    void cleanLogs();

    void recordLogininfor(LogininforDTO logininfor);

    long count();

    long countByStatus(String status);
}
