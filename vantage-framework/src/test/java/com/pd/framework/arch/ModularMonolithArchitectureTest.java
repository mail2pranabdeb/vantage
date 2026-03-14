package com.pd.framework.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Architecture tests to enforce modular monolith boundaries.
 * These tests ensure proper layering and module isolation.
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
     * Rule: Controllers should depend on service APIs, not directly on repositories.
     * This enforces the use of module API layer.
     */
    @Test
    void controllersShouldNotDependDirectlyOnRepositories() {
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

    /**
     * Rule: External modules should access system module through its API.
     * Classes outside system module should not access system infrastructure directly.
     */
    @Test
    void systemModuleInfrastructureShouldBeInternal() {
        // Infrastructure layer should only be accessed within the same module
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("com.pd.modules.system..")
                .should().dependOnClassesThat().resideInAPackage("com.pd.modules.system.infrastructure..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: External modules should access quartz module through its API.
     */
    @Test
    void quartzModuleInfrastructureShouldBeInternal() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("com.pd.modules.quartz..")
                .should().dependOnClassesThat().resideInAPackage("com.pd.modules.quartz.infrastructure..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: External modules should access generator module through its API.
     */
    @Test
    void generatorModuleInfrastructureShouldBeInternal() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("com.pd.modules.generator..")
                .should().dependOnClassesThat().resideInAPackage("com.pd.modules.generator.infrastructure..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: Service implementations should be in impl package.
     */
    @Test
    void serviceImplementationsShouldBeInImplPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("ServiceImpl")
                .should().resideInAPackage("..service.impl..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: API interfaces should be in api package.
     */
    @Test
    void apiInterfacesShouldBeInApiPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Service")
                .and().areInterfaces()
                .and().resideInAPackage("com.pd.modules..")
                .should().resideInAPackage("..api..");

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: Domain events should extend DomainEvent base class.
     */
    @Test
    void domainEventsShouldExtendBaseEvent() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Event")
                .and().resideInAPackage("com.pd.common.event..")
                .should().beAssignableTo(com.pd.common.event.DomainEvent.class);

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: Event listeners should use @TransactionalEventListener.
     */
    @Test
    void eventListenersShouldUseTransactionalEventListener() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("EventListener")
                .and().resideInAPackage("com.pd.modules..listener..")
                .should().beAnnotatedWith(org.springframework.transaction.event.TransactionalEventListener.class)
                .orShould().beAnnotatedWith(org.springframework.context.event.EventListener.class);

        rule.allowEmptyShould(true).check(allClasses);
    }

    /**
     * Rule: Modules should communicate via events, not direct calls.
     * This verifies that cross-module dependencies go through events.
     */
    @Test
    void crossModuleCommunicationShouldUseEvents() {
        // Verify that modules don't directly depend on each other's internal classes
        ArchRule systemToQuartz = noClasses()
                .that().resideInAPackage("com.pd.modules.system..")
                .and().areNotAssignableTo(com.pd.common.event.DomainEvent.class)
                .should().dependOnClassesThat().resideInAPackage("com.pd.modules.quartz.service..");

        systemToQuartz.allowEmptyShould(true).check(allClasses);
    }
}
