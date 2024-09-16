plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-auth:2.3.12")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.12")
    implementation("io.ktor:ktor-server-http-redirect:2.3.12")
    implementation("io.ktor:ktor-server-forwarded-header:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    implementation("io.ktor:ktor-server-status-pages:2.3.12")
    implementation("io.ktor:ktor-server-websockets:2.3.12")
    implementation("io.ktor:ktor-server-call-logging:2.3.12")
    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.12")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.3.12")
    implementation("io.micrometer:micrometer-registry-prometheus:1.13.4")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("com.webauthn4j:webauthn4j-core:0.26.0.RELEASE")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
