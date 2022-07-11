val arrowVersion: String by project

plugins {
    kotlin("jvm")
    `java-test-fixtures`
}

dependencies {
    testFixturesImplementation("io.arrow-kt:arrow-core:$arrowVersion")
    testFixturesImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}