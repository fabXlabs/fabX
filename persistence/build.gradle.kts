val kotlinxCoroutinesVersion: String by project
val exposedVersion: String by project
val postgresDriverVersion: String by project
val liquibaseVersion: String by project
val testcontainersVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.21"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")
    implementation("com.mattbertolini:liquibase-slf4j:4.1.0")
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
}