
plugins {
    java
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.cloud.tools.jib") version "3.4.5"
}

group = "com.opencqrs"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jib {
    to {
        image = "ghcr.io/open-cqrs/opencqrs-samples-sagas:latest"
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.integration:spring-integration-jdbc")
    runtimeOnly("com.h2database:h2")
    implementation("com.opencqrs:framework-spring-boot-starter:1.0-rc1")
    testImplementation("com.opencqrs:framework-test:1.0-rc1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:2.1.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}