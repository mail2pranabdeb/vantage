package com.pd.admin;

import com.pd.VantageAdminApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Modulith verification and documentation generation tests.
 * Verifies module boundaries, dependencies, and generates C4 documentation.
 */
class ModulithTests {

    static ApplicationModules modules = ApplicationModules.of(VantageAdminApplication.class);

    @Test
    @DisplayName("Module structure is valid")
    void verifyModuleStructure() {
        // Print module structure
        modules.forEach(System.out::println);

        // Verify expected modules exist
        long systemCount = modules.stream().filter(m -> m.getDisplayName().equals("System")).count();
        long quartzCount = modules.stream().filter(m -> m.getDisplayName().equals("Quartz")).count();
        long generatorCount = modules.stream().filter(m -> m.getDisplayName().equals("Generator")).count();
        long commonCount = modules.stream().filter(m -> m.getDisplayName().equals("Common")).count();

        assertThat(systemCount).isGreaterThan(0);
        assertThat(quartzCount).isGreaterThan(0);
        assertThat(generatorCount).isGreaterThan(0);
        assertThat(commonCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("Module dependencies are valid")
    void verifyModuleDependencies() {
        // Verify modules can be verified (no circular dependencies, no illegal access)
        modules.verify();
    }

    @Test
    @DisplayName("Generate C4 documentation")
    void generateC4Documentation() {
        new Documenter(modules).writeDocumentation();
    }
}
