val arrowVersion: String by project
val kotlinxDateTimeVersion: String by project

plugins {
    kotlin("jvm")
    `java-test-fixtures`
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")

    testFixturesImplementation("io.arrow-kt:arrow-core:$arrowVersion")
    testFixturesImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}