val kotlinxCoroutinesVersion: String by project
val ktorVersion: String by rootProject
val logbackVersion: String by rootProject
val slf4jVersion: String by project
val webauthn4jVersion: String by project
val micrometerVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.21"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-http-redirect:$ktorVersion")
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.webauthn4j:webauthn4j-core:$webauthn4jVersion")
    implementation("io.ktor:ktor-server-core-jvm:2.3.0")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.2.3")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
}