val exposedVersion: String by project
val postgresDriverVersion: String by project
val testcontainersVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.10"
}

dependencies {
    implementation(project(":domain"))
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
}