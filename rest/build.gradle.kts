val ktorVersion: String by rootProject
val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.0"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}