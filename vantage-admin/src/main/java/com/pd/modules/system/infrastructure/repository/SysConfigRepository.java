package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    @Query("SELECT c FROM SysConfig c WHERE c.configKey = :configKey")
    Optional<SysConfig> findByConfigKey(@Param("configKey") String configKey);

    @Query("SELECT c FROM SysConfig c ORDER BY c.configName ASC")
    List<SysConfig> findAllActive();

    @Query("SELECT c FROM SysConfig c WHERE c.configType = :configType")
    List<SysConfig> findByConfigType(@Param("configType") String configType);
}
