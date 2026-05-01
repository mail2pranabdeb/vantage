package com.pd.modules.quartz.api;

import java.util.List;
import java.util.Map;

/**
 * Quartz module public API for script job execution.
 */
public interface QuartzScriptJobService {

    List<Map<String, String>> runScript(String scriptType, String scriptContent);
}
