plugins {
    kotlin("jvm")
    `java-test-fixtures`
    kotlin("plugin.serialization") version "1.9.22"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("com.webauthn4j:webauthn4j-core:0.22.0.RELEASE")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testFixturesImplementation("io.arrow-kt:arrow-core:1.2.1")
    testFixturesImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.0")
    testFixturesImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testFixturesImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}
