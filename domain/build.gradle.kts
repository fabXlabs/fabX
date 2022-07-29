val arrowVersion: String by project
val assertKVersion: String by project

plugins {
    kotlin("jvm")
    `java-test-fixtures`
    kotlin("plugin.serialization") version "1.6.10"
}

dependencies {
    testFixturesImplementation("io.arrow-kt:arrow-core:$arrowVersion")
    testFixturesImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}