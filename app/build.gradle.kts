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
}