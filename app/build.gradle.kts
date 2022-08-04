val ktorVersion: String by rootProject
val kotlinxDateTimeVersion: String by project
val kotlinxCoroutinesVersion: String by project
val testcontainersVersion: String by project

val exposedVersion: String by project

plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
    mainClass.set("cloud.fabX.fabXaccess.AppKt")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "cloud.fabX.fabXaccess.AppKt"))
        }
    }
}

dependencies {
    implementation(project(":web"))
    implementation(project(":frontend"))
    implementation(project(":domain"))
    implementation(project(":persistence"))
    implementation(project(":logging"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("io.ktor:ktor-serialization:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
}
