val kotlinxCoroutinesVersion: String by project
val webauthn4jVersion: String by project

val arrowVersion: String by project
val assertKVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunitVersion: String by project

plugins {
    kotlin("jvm")
    `java-test-fixtures`
    kotlin("plugin.serialization") version "1.7.10"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("com.webauthn4j:webauthn4j-core:$webauthn4jVersion")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
    testFixturesImplementation("io.arrow-kt:arrow-core:$arrowVersion")
    testFixturesImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
    testFixturesImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testFixturesImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitVersion")
}

kotlin.target.compilations.getByName("testFixtures") {
    associateWith(target.compilations.getByName("main"))
}