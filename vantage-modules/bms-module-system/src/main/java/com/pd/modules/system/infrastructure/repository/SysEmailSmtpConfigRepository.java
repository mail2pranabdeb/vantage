package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysEmailSmtpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysEmailSmtpConfigRepository extends JpaRepository<SysEmailSmtpConfig, Long> {

    @Query("SELECT c FROM SysEmailSmtpConfig c WHERE c.status = '0' ORDER BY c.configId ASC")
    Optional<SysEmailSmtpConfig> findActiveConfig();
}
