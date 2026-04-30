package com.pd.gateway.quartz;

import com.pd.common.core.controller.BaseController;
import com.pd.common.core.domain.AjaxResult;
import com.pd.modules.quartz.domain.SysJob;
import com.pd.modules.quartz.service.JobTemplateService;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Job template controller
 */
@RestController
@RequestMapping("/api/system/job-template")
public class JobTemplateController extends BaseController {

    @Autowired
    private JobTemplateService jobTemplateService;

    @Autowired
    private com.pd.modules.quartz.service.ISysJobService sysJobService;

    /**
     * Get all templates
     */
    @GetMapping("/list")
    public AjaxResult list() {
        return success(jobTemplateService.getTemplates());
    }

    /**
     * Get template by name
     */
    @GetMapping("/{name}")
    public AjaxResult getTemplate(@PathVariable String name) {
        return jobTemplateService.getTemplateByName(name)
                .map(this::success)
                .orElse(error("Template not found"));
    }

    /**
     * Create job from template
     */
    @PostMapping("/create/{templateName}")
    public AjaxResult createFromTemplate(
            @PathVariable String templateName,
            @RequestParam(required = false) String jobName) throws SchedulerException {
        SysJob job = jobTemplateService.createJobFromTemplate(templateName, jobName);
        sysJobService.insertJob(job);
        return success("Job created from template: " + templateName);
    }
}
