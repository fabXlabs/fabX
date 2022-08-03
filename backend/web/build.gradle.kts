val ktorVersion: String by rootProject
val kotlinxCoroutinesVersion: String by project
val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.6.10"

    id("com.github.node-gradle.node") version "3.2.1"
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-single-page:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

node {
    download.set(false)
    npmInstallCommand.set("ci")
    nodeProjectDir.set(file("${project.rootDir}/../frontend/"))
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    outputs.upToDateWhen {
        false
    }
}

tasks.register<Copy>("copyFrontend") {
    dependsOn("npmBuild")
    from(file("${project.rootDir}/../frontend/dist/fabx-dashboard/"))
    into(file("${project.sourceSets.main.get().output.resourcesDir}/frontend/"))
}

tasks.withType<Jar>().configureEach {
    dependsOn("copyFrontend")
}