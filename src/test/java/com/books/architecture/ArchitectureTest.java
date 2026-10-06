package com.books.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/** Clean-architecture rules. A violation fails the build. */
@AnalyzeClasses(packages = "com.books", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	private static final String[] FRAMEWORKS = { "org.springframework..", "jakarta.persistence..", "jakarta.ws.rs..",
			"jakarta.servlet..", "org.hibernate..", "tools.jackson..", "com.fasterxml..",
			"org.mapstruct..", "io.swagger..", "com.platform.." };

	@ArchTest
	static final ArchRule dependenciesPointInward = layeredArchitecture().consideringOnlyDependenciesInLayers()
			.layer("Domain").definedBy("com.books.domain..")
			.layer("Application").definedBy("com.books.application..")
			.layer("Infrastructure").definedBy("com.books.infrastructure..")
			.layer("Interfaces").definedBy("com.books.interfaces..")
			.whereLayer("Interfaces").mayNotBeAccessedByAnyLayer()
			.whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
			.whereLayer("Application").mayOnlyBeAccessedByLayers("Interfaces", "Infrastructure")
			.whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Interfaces");

	@ArchTest
	static final ArchRule domainIsPureJava = noClasses().that().resideInAPackage("com.books.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(FRAMEWORKS)
			.because("the domain must not know Spring, JPA, Keycloak or any other framework");

	@ArchTest
	static final ArchRule domainDependsOnNothingElse = classes().that().resideInAPackage("com.books.domain..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("com.books.domain..", "java..");

	@ArchTest
	static final ArchRule applicationDependsOnlyOnDomain = classes().that().resideInAPackage("com.books.application..")
			.should().onlyDependOnClassesThat().resideInAnyPackage("com.books.application..", "com.books.domain..", "java..");

	@ArchTest
	static final ArchRule platformCallsStayInTheirAdapter = noClasses().that()
			.resideOutsideOfPackage("com.books.infrastructure.platform..")
			.should().dependOnClassesThat().resideInAnyPackage("org.springframework.web.client..",
					"org.springframework.cloud.client..")
			.because("how users are looked up on the platform must only touch that adapter");

	@ArchTest
	static final ArchRule jpaStaysInPersistence = noClasses().that()
			.resideOutsideOfPackage("com.books.infrastructure.persistence..")
			.should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "org.springframework.data..");

	@ArchTest
	static final ArchRule controllersCallUseCasesNotPorts = noClasses().that().resideInAPackage("com.books.interfaces..")
			.should().dependOnClassesThat().resideInAPackage("com.books.domain.port..")
			.because("the interfaces layer goes through application use cases");
}
