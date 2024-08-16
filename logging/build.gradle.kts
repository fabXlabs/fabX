plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))

    implementation("ch.qos.logback:logback-classic:1.5.7")
}
