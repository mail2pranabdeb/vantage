package com.pd.modules.system.infrastructure.repository;

import com.pd.modules.system.domain.SysDictData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysDictDataRepository extends JpaRepository<SysDictData, Long> {

    @Query("SELECT d FROM SysDictData d WHERE d.dictType = :dictType ORDER BY d.dictSort")
    List<SysDictData> findByDictTypeOrderBySort(String dictType);

    @Query("SELECT d FROM SysDictData d WHERE d.dictType = :dictType AND d.status = '0' ORDER BY d.dictSort")
    List<SysDictData> findActiveByDictTypeOrderBySort(String dictType);

    @Query("SELECT d FROM SysDictData d WHERE d.dictType = :dictType AND d.dictValue = :dictValue")
    Optional<SysDictData> findByDictTypeAndDictValue(String dictType, String dictValue);

    void deleteByDictType(String dictType);

    @Query("SELECT d FROM SysDictData d WHERE d.dictType = :dictType AND d.status = :status ORDER BY d.dictSort")
    List<SysDictData> findByDictTypeAndStatusOrderByDictSort(String dictType, String status);
}