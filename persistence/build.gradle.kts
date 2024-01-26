plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-json:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.liquibase:liquibase-core:4.25.1")
    implementation("com.mattbertolini:liquibase-slf4j:5.0.0")
    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.testcontainers:testcontainers:1.19.4")
    testImplementation("org.testcontainers:postgresql:1.19.4")
}
