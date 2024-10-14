plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation(rootProject.libs.kotlinx.coroutines.core)
    implementation(rootProject.libs.exposed.core)
    implementation(rootProject.libs.exposed.json)
    implementation(rootProject.libs.exposed.jdbc)
    implementation(rootProject.libs.exposed.java.time)
    implementation(rootProject.libs.hikaricp)
    implementation(rootProject.libs.postgresql)
    implementation(rootProject.libs.liquibase)
    implementation(rootProject.libs.liquibase.slf4j)
    implementation(rootProject.libs.kotlinx.coroutines.test)

    testImplementation(testFixtures(project(":domain")))
    testImplementation(rootProject.libs.kotlinx.coroutines.test)
    testImplementation(rootProject.libs.testcontainers)
    testImplementation(rootProject.libs.testcontainers.postgres)
}