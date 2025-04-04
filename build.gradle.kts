plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
}

group = "com.loopy"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot core
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	
	// Configuration processor
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	
	// AMQP và RabbitMQ
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	
	// OpenAPI/Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
	
	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
	
	// Redis (cho caching)
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	
	// S3 Client cho MinIO
	implementation("io.minio:minio:8.5.7")
	
	// AWS SDK S3 cho Cloudflare R2
	implementation("software.amazon.awssdk:s3:2.20.26")
	
	// Hibernate Types for PostgreSQL JSONB support
	implementation("com.vladmihalcea:hibernate-types-60:2.21.1")
	
	// Database
	runtimeOnly("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql:10.10.0")
	
	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	
	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.springframework.amqp:spring-rabbit-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}

jacoco {
	toolVersion = "0.8.11"
}
