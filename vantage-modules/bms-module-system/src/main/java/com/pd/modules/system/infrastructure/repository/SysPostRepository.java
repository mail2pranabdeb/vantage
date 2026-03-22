package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysPostRepository extends JpaRepository<SysPost, Long> {

    @Query("SELECT p FROM SysPost p WHERE p.postCode = :postCode")
    Optional<SysPost> findByPostCode(@Param("postCode") String postCode);

    @Query("SELECT p FROM SysPost p ORDER BY p.postSort ASC")
    List<SysPost> findAllActive();

    @Query("SELECT p FROM SysPost p WHERE p.status = :status")
    List<SysPost> findByStatus(@Param("status") String status);
}
