package com.pd.modules.quartz.infrastructure.repository;

import com.pd.modules.quartz.domain.SysJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysJobRepository extends JpaRepository<SysJob, Long> {

    @Query("SELECT j FROM SysJob j WHERE j.status = :status ORDER BY j.createTime DESC")
    List<SysJob> findByStatus(@Param("status") String status);

    @Query("SELECT j FROM SysJob j WHERE j.jobGroup = :jobGroup ORDER BY j.jobName ASC")
    List<SysJob> findByJobGroup(@Param("jobGroup") String jobGroup);

    @Query("SELECT j FROM SysJob j ORDER BY j.createTime DESC")
    List<SysJob> findAllActive();

    @Query("SELECT j FROM SysJob j WHERE (:jobName IS NULL OR j.jobName LIKE CONCAT('%', :jobName, '%')) AND (:jobGroup IS NULL OR j.jobGroup = :jobGroup) AND (:status IS NULL OR j.status = :status)")
    Page<SysJob> searchJobs(@Param("jobName") String jobName, @Param("jobGroup") String jobGroup, @Param("status") String status, Pageable pageable);

    List<SysJob> findByJobNameContainingIgnoreCase(String jobName);

    long countByStatus(String status);
}
