plugins {
	java
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.schmhj"
version = "0.0.1-SNAPSHOT"
description = "Opinionated, production-ready Spring Boot 4 microservice template using Domain-Driven Design (DDD) and Clean Architecture principles. Cloud-native, provider-agnostic, and focused on core microservice needs."

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2025.1.0"
extra["archunitVersion"] = "1.4.1"
extra["logstashVersion"] = "9.0"
extra["braveVersion"] = "1.6.1"
extra["springdocVersion"] = "3.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
	implementation("net.logstash.logback:logstash-logback-encoder:${property("logstashVersion")}")
	implementation("io.micrometer:micrometer-tracing-bridge-brave:${property("braveVersion")}")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("com.tngtech.archunit:archunit-junit5:${property("archunitVersion")}")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		showStandardStreams = true
		events("passed", "skipped", "failed")
	}
}
