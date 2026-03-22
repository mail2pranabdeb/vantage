package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysLogininfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SysLogininfor entity
 */
@Repository
public interface SysLogininforRepository extends JpaRepository<SysLogininfor, Long> {

    @Query("SELECT l FROM SysLogininfor l WHERE 1=1 " +
           "AND (:loginName IS NULL OR l.loginName LIKE %:loginName%) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:ipaddr IS NULL OR l.ipaddr LIKE %:ipaddr%)")
    List<SysLogininfor> findByCondition(@Param("loginName") String loginName,
                                        @Param("status") String status,
                                        @Param("ipaddr") String ipaddr);
}
