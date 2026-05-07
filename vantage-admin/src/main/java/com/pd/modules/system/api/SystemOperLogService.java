package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.OperLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SystemOperLogService {

    List<OperLogDTO> findAll();

    List<OperLogDTO> findRecent(int limit);

    Optional<OperLogDTO> findById(Long operId);

    List<OperLogDTO> findByOperName(String operName);

    List<OperLogDTO> findByTitle(String title);

    List<OperLogDTO> findByCondition(String title, String operName, Integer businessType, Integer status);

    Page<OperLogDTO> findByConditionPaginated(String title, String operName, Integer businessType, Integer status, Pageable pageable);

    boolean deleteByIds(Long[] operIds);

    void cleanLogs();

    long count();

    long countByStatus(String status);
}
