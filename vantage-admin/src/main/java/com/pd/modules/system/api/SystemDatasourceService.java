package com.pd.modules.system.api;

import com.pd.modules.system.api.dto.DatasourceDTO;
import java.util.List;
import java.util.Optional;

public interface SystemDatasourceService {

    List<DatasourceDTO> findAll();

    Optional<DatasourceDTO> findById(Long datasourceId);

    DatasourceDTO save(DatasourceDTO datasource);

    void deleteById(Long datasourceId);

    boolean existsByDatasourceKey(String datasourceKey);

    boolean testConnection(DatasourceDTO datasource);

    String getDriverClass(String dbType);

    String getDefaultUrlPattern(String dbType);
}
