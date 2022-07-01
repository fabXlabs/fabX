val logbackVersion: String by rootProject

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":domain"))

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
}