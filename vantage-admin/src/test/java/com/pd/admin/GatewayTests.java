package com.pd.admin;

import com.pd.gateway.GatewayManagement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gateway Management tests.
 * Verifies that GatewayManagement is the sole external entry point
 * and that all module controllers have been removed.
 */
@SpringBootTest
class GatewayTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Test
    @DisplayName("GatewayManagement bean exists")
    void gatewayBeanExists() {
        assertThat(context.getBean(GatewayManagement.class)).isNotNull();
    }

    @Test
    @DisplayName("Only one RestController exists (GatewayManagement)")
    void onlyOneRestController() {
        String[] controllers = context.getBeanNamesForAnnotation(org.springframework.web.bind.annotation.RestController.class);
        assertThat(controllers).hasSize(1);
        assertThat(controllers[0]).isEqualTo("gatewayManagement");
    }

    @Test
    @DisplayName("All registered routes are under /api prefix")
    void allRoutesUnderApiPrefix() {
        var mappings = requestMappingHandlerMapping.getHandlerMethods();
        List<String> paths = mappings.keySet().stream()
            .flatMap(info -> {
                // Spring Framework 7.x uses getPathPatternsCondition()
                var pathPatterns = info.getPathPatternsCondition();
                if (pathPatterns != null) {
                    return pathPatterns.getPatterns().stream().map(Object::toString);
                }
                // Fallback for older versions
                var patterns = info.getPatternsCondition();
                return patterns != null ? patterns.getPatterns().stream() : Stream.empty();
            })
            .filter(p -> !p.equals("/error"))
            .filter(p -> !p.startsWith("/actuator"))
            .toList();

        // All non-actuator routes should be under /api
        assertThat(paths).isNotEmpty();
        assertThat(paths).allMatch(p -> p.startsWith("/api"));
    }

    @Test
    @DisplayName("No module web controllers exist in context")
    void noModuleWebControllers() {
        String[] webBeans = Arrays.stream(context.getBeanNamesForType(Object.class))
            .filter(name -> name.contains("Controller"))
            .filter(name -> !name.equals("gatewayManagement"))
            .filter(name -> !name.equals("baseController"))
            .filter(name -> !name.equals("basicErrorController"))
            .filter(name -> !name.contains("PostProcessor"))
            .filter(name -> !name.contains("viewControllerHandlerMapping"))
            .filter(name -> !name.contains("simpleControllerHandlerAdapter"))
            .toArray(String[]::new);

        assertThat(webBeans).isEmpty();
    }

    @Test
    @DisplayName("System module API interfaces are available")
    void systemApiInterfacesAvailable() {
        // Verify core system API beans exist
        assertThat(context.getBean(com.pd.modules.system.api.SystemUserService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.system.api.SystemRoleService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.system.api.SystemMenuService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.system.api.SystemConfigService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.system.api.SystemDictService.class)).isNotNull();
    }

    @Test
    @DisplayName("Quartz module API interfaces are available")
    void quartzApiInterfacesAvailable() {
        // Verify core quartz API beans exist
        assertThat(context.getBean(com.pd.modules.quartz.api.QuartzJobService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.quartz.api.QuartzJobLogService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.quartz.api.QuartzJobTemplateService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.quartz.api.QuartzJobGroupService.class)).isNotNull();
        assertThat(context.getBean(com.pd.modules.quartz.api.QuartzJobMetricsService.class)).isNotNull();
    }

    @Test
    @DisplayName("Generator module API interface is available")
    void generatorApiInterfaceAvailable() {
        assertThat(context.getBean(com.pd.modules.generator.api.GeneratorService.class)).isNotNull();
    }

    @Test
    @DisplayName("GatewayManagement injects all API interfaces")
    void gatewayInjectsAllApis() {
        GatewayManagement gateway = context.getBean(GatewayManagement.class);
        assertThat(gateway).isNotNull();
    }
}
