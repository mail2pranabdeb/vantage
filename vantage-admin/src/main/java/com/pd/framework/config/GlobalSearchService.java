package com.pd.framework.config;

import com.pd.modules.quartz.api.QuartzJobService;
import com.pd.modules.quartz.api.dto.JobDTO;
import com.pd.modules.system.api.*;
import com.pd.modules.system.api.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GlobalSearchService {

    private final SystemUserService systemUserService;
    private final SystemRoleService systemRoleService;
    private final SystemMenuService systemMenuService;
    private final SystemConfigService systemConfigService;
    private final SystemNoticeService systemNoticeService;
    private final QuartzJobService quartzJobService;

    public GlobalSearchService(SystemUserService systemUserService,
                               SystemRoleService systemRoleService,
                               SystemMenuService systemMenuService,
                               SystemConfigService systemConfigService,
                               SystemNoticeService systemNoticeService,
                               QuartzJobService quartzJobService) {
        this.systemUserService = systemUserService;
        this.systemRoleService = systemRoleService;
        this.systemMenuService = systemMenuService;
        this.systemConfigService = systemConfigService;
        this.systemNoticeService = systemNoticeService;
        this.quartzJobService = quartzJobService;
    }

    public List<Map<String, Object>> search(String query, int maxPerType) {
        String q = query.toLowerCase().trim();
        List<Map<String, Object>> results = new ArrayList<>();
        results.addAll(searchUsers(q, maxPerType));
        results.addAll(searchRoles(q, maxPerType));
        results.addAll(searchMenus(q, maxPerType));
        results.addAll(searchConfigs(q, maxPerType));
        results.addAll(searchNotices(q, maxPerType));
        results.addAll(searchJobs(q, maxPerType));
        return results;
    }

    private List<Map<String, Object>> searchUsers(String q, int max) {
        return systemUserService.findAll().stream()
                .filter(u -> matches(q, u.getLoginName(), u.getUserName(), u.getEmail(), u.getPhonenumber()))
                .limit(max)
                .map(u -> result("User", u.getUserId(), u.getUserName(), "/system/user", u.getLoginName()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> searchRoles(String q, int max) {
        return systemRoleService.findAllActive().stream()
                .filter(r -> matches(q, r.getRoleName(), r.getRoleKey()))
                .limit(max)
                .map(r -> result("Role", r.getRoleId(), r.getRoleName(), "/system/role", r.getRoleKey()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> searchMenus(String q, int max) {
        return systemMenuService.findAllMenus().stream()
                .filter(m -> matches(q, m.getMenuName(), m.getPerms(), m.getUrl()))
                .limit(max)
                .map(m -> result("Menu", m.getMenuId(), m.getMenuName(), "/system/menu", m.getPerms()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> searchConfigs(String q, int max) {
        return systemConfigService.findAll().stream()
                .filter(c -> matches(q, c.getConfigName(), c.getConfigKey(), c.getConfigValue()))
                .limit(max)
                .map(c -> result("Config", c.getConfigId(), c.getConfigName(), "/system/config", c.getConfigKey()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> searchNotices(String q, int max) {
        return systemNoticeService.findActiveNotices().stream()
                .filter(n -> matches(q, n.getNoticeTitle(), n.getNoticeContent()))
                .limit(max)
                .map(n -> result("Notice", n.getNoticeId(), n.getNoticeTitle(), "/system/notice", ""))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> searchJobs(String q, int max) {
        return quartzJobService.findAll().stream()
                .filter(j -> matches(q, j.getJobName(), j.getJobGroup(), j.getInvokeTarget(), j.getCronExpression()))
                .limit(max)
                .map(j -> result("Job", j.getJobId(), j.getJobName(), "/system/job", j.getJobGroup()))
                .collect(Collectors.toList());
    }

    private boolean matches(String query, String... fields) {
        for (String f : fields) {
            if (f != null && f.toLowerCase().contains(query)) return true;
        }
        return false;
    }

    private Map<String, Object> result(String type, Object id, String label, String url, String subtitle) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", type);
        r.put("id", id);
        r.put("label", label != null ? label : "");
        r.put("url", url);
        r.put("subtitle", subtitle != null ? subtitle : "");
        return r;
    }
}
