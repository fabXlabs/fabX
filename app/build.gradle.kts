val kotlinxDateTimeVersion: String by project

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
}