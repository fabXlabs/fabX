plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))
    testImplementation(testFixtures(project(":domain")))
}