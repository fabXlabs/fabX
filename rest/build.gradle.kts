plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:1.6.0")
    implementation("io.ktor:ktor-server-netty:1.6.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
}