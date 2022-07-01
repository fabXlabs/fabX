plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":rest"))
    implementation(project(":domain"))
    implementation(project(":persistence"))
    implementation(project(":logging"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
}