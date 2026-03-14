package com.pd.framework.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Architecture tests to enforce modular monolith boundaries.
 */
class ModularMonolithArchitectureTest {

    private final JavaClasses allClasses = new ClassFileImporter()
            .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
            .importPackages("com.pd");

    /**
     * Rule: Framework layer should not depend on business modules.
     */
    @Test
    void frameworkShouldNotDependOnModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.pd.framework..")
                .should().dependOnClassesThat().resideInAnyPackage("com.pd.modules..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: Domain classes should not depend on web layer.
     */
    @Test
    void domainClassesShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.pd.modules..domain..")
                .should().dependOnClassesThat().resideInAPackage("com.pd.modules..web..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: Controllers should not depend directly on repositories (should go through services).
     */
    @Test
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.pd.modules..web..")
                .should().dependOnClassesThat().resideInAPackage("com.pd.modules..infrastructure..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: All controllers should have @RestController or @Controller annotation.
     */
    @Test
    void controllersShouldHaveControllerAnnotation() {
        ArchRule rule = classes()
                .that().resideInAPackage("com.pd.modules..web..")
                .and().haveSimpleNameEndingWith("Controller")
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .orShould().beAnnotatedWith(org.springframework.stereotype.Controller.class);

        rule.allowEmptyShould(true).check(allClasses);
    }
}
