package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysDictType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysDictTypeRepository extends JpaRepository<SysDictType, Long> {

    @Query("SELECT d FROM SysDictType d WHERE d.dictType = :dictType")
    Optional<SysDictType> findByDictType(@Param("dictType") String dictType);

    @Query("SELECT d FROM SysDictType d ORDER BY d.createTime DESC")
    List<SysDictType> findAllActive();

    @Query("SELECT d FROM SysDictType d WHERE d.status = :status")
    List<SysDictType> findByStatus(@Param("status") String status);
}
