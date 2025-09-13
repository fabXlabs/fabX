plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-test-fixtures`
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(rootProject.libs.kotlinx.coroutines.core)
    implementation(rootProject.libs.webauthn4j.core)
    implementation(rootProject.libs.scrimage.core)

    testImplementation(rootProject.libs.kotlinx.coroutines.test)

    testFixturesImplementation(rootProject.libs.webauthn4j.core)
    testFixturesImplementation(rootProject.libs.arrow.core)
    testFixturesImplementation(rootProject.libs.assertk.jvm)
    testFixturesImplementation(rootProject.libs.mockito.kotlin)
    testFixturesImplementation(rootProject.libs.mockito.junit5)
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}
