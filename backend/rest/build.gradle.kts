val ktorVersion: String by rootProject
val kotlinxCoroutinesVersion: String by project
val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.10"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}