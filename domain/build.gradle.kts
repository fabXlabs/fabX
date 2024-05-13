plugins {
    kotlin("jvm")
    `java-test-fixtures`
    kotlin("plugin.serialization") version "1.9.24"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.webauthn4j:webauthn4j-core:0.24.0.RELEASE")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testFixturesImplementation("io.arrow-kt:arrow-core:1.2.4")
    testFixturesImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testFixturesImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    testFixturesImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}
