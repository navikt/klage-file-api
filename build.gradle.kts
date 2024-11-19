import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val gcsVersion = "2.45.0"
val logstashVersion = "8.0"
val tokenValidationVersion = "5.0.11"

repositories {
    mavenCentral()
}

plugins {
    val kotlinVersion = "2.0.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "3.3.5"
    idea
}

java.sourceCompatibility = JavaVersion.VERSION_21

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.projectreactor:reactor-spring:1.0.1.RELEASE")

    implementation("io.micrometer:micrometer-registry-prometheus")

    implementation("ch.qos.logback:logback-classic")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")

    implementation("com.google.cloud:google-cloud-storage:$gcsVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "org.junit.vintage")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions{
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}
