package com.pd.framework.config;

import java.util.List;
import java.util.Map;

public record ExportRequest(
    List<ExportService.Column> columns,
    List<Map<String, Object>> rows
) {}
