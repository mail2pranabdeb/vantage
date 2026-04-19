package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysNoticeRepository extends JpaRepository<SysNotice, Integer> {

    @Query("SELECT n FROM SysNotice n WHERE n.noticeType = :noticeType ORDER BY n.createTime DESC")
    List<SysNotice> findByNoticeType(@Param("noticeType") String noticeType);

    @Query("SELECT n FROM SysNotice n WHERE n.status = :status ORDER BY n.createTime DESC")
    List<SysNotice> findByStatus(@Param("status") String status);

    @Query("SELECT n FROM SysNotice n ORDER BY n.createTime DESC")
    List<SysNotice> findAllActive();
}
