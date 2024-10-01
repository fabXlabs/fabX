plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-json:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")
    implementation("com.zaxxer:HikariCP:6.0.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.liquibase:liquibase-core:4.29.2")
    implementation("com.mattbertolini:liquibase-slf4j:5.0.0")
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.testcontainers:testcontainers:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")
}
