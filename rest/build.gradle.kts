val ktorVersion: String by rootProject
val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}