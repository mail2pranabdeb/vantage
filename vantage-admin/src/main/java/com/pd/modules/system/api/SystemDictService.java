package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.DictDataDTO;
import com.pd.modules.system.api.dto.DictTypeDTO;
import java.util.List;
import java.util.Optional;

/**
 * System module public API for dictionary operations.
 */
public interface SystemDictService {

    // Dict Type operations
    List<DictTypeDTO> findAllTypes();

    List<DictTypeDTO> findAllActiveTypes();

    Optional<DictTypeDTO> findTypeById(Long dictId);

    Optional<DictTypeDTO> findTypeByDictType(String dictType);

    DictTypeDTO createType(DictTypeDTO dictType);

    DictTypeDTO updateType(DictTypeDTO dictType);

    boolean deleteTypeByIds(Long[] dictIds);

    boolean deleteTypeById(Long dictId);

    boolean existsByDictType(String dictType);

    // Dict Data operations
    List<DictDataDTO> findDataByType(String dictType);

    List<DictDataDTO> findDataByTypeOrderBySort(String dictType);

    List<DictDataDTO> findAllData();

    Optional<DictDataDTO> findDataById(Long dictCode);

    DictDataDTO createData(DictDataDTO dictData);

    DictDataDTO updateData(DictDataDTO dictData);

    boolean deleteDataByIds(Long[] dictCodes);

    boolean deleteDataById(Long dictCode);
}
