val ktorVersion: String by rootProject
val kotlinxDateTimeVersion: String by project

val exposedVersion: String by project

plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("cloud.fabX.fabXaccess.AppKt")
}

dependencies {
    implementation(project(":rest"))
    implementation(project(":domain"))
    implementation(project(":persistence"))
    implementation(project(":logging"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    testImplementation(testFixtures(project(":domain")))
    testImplementation("io.ktor:ktor-serialization:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    // TODO remove with database migration tool
    testImplementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
}