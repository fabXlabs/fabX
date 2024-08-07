plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("io.ktor:ktor-serialization:2.3.12")
    testImplementation("io.ktor:ktor-server-test-host:2.3.12")
    testImplementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.testcontainers:testcontainers:1.20.1")
    testImplementation("org.testcontainers:postgresql:1.20.1")
    testImplementation("org.jetbrains.exposed:exposed-core:0.53.0")
    testImplementation("com.zaxxer:HikariCP:5.1.0")
}
