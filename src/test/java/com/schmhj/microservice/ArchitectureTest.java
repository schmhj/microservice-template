package com.schmhj.microservice;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.schmhj.microservice")
public class ArchitectureTest {

        // 1. Define and check Layered Architecture
        @ArchTest
        static final ArchRule layers_should_respect_hexagonal_boundaries = layeredArchitecture()
                        .consideringAllDependencies()
                        .layer("Domain").definedBy("..domain..")
                        .layer("Application").definedBy("..application..")
                        .layer("Infrastructure").definedBy("..infrastructure..")

                        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                        .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                        .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                        .allowEmptyShould(true);

        // 2. Ensure Domain does not depend on Frameworks (Spring, Persistence, etc.)
        @ArchTest
        static final ArchRule domain_should_not_depend_on_frameworks = noClasses().that().resideInAPackage("..domain..")
                        .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                        .orShould().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                        .orShould().dependOnClassesThat().resideInAPackage("jakarta.transaction..")
                        .orShould().dependOnClassesThat().resideInAPackage("io.swagger.v3.oas.annotations..")
                        .allowEmptyShould(true);

        // 3. Ensure Ports are interfaces (Best practice for Ports)
        @ArchTest
        static final ArchRule ports_should_be_interfaces = classes().that().resideInAPackage("..application.port..")
                        .should().beInterfaces()
                        .allowEmptyShould(true);

        // 4. Ensure Adapters stay in Infrastructure
        @ArchTest
        static final ArchRule adapters_should_only_reside_in_infrastructure = noClasses().that()
                        .resideOutsideOfPackage("..infrastructure.adapter..")
                        .should().haveSimpleNameEndingWith("Adapter")
                        .orShould().haveSimpleNameEndingWith("Controller")
                        .orShould().haveSimpleNameEndingWith("Decorator")
                        .allowEmptyShould(true);
}