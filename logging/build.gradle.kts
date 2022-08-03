val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}