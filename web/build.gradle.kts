plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":logging"))

    implementation(rootProject.libs.ktor.server.core)
    implementation(rootProject.libs.ktor.server.core.jvm)
    implementation(rootProject.libs.ktor.server.netty)
    implementation(rootProject.libs.ktor.server.auth)
    implementation(rootProject.libs.ktor.server.auth.jwt)
    implementation(rootProject.libs.ktor.server.http.redirect)
    implementation(rootProject.libs.ktor.server.forwarded.header)
    implementation(rootProject.libs.ktor.server.content.negotiation)
    implementation(rootProject.libs.ktor.serialization.kotlinx.json)
    implementation(rootProject.libs.ktor.server.cors)
    implementation(rootProject.libs.ktor.server.call.logging)
    implementation(rootProject.libs.ktor.server.http.redirect)
    implementation(rootProject.libs.ktor.server.status.pages)
    implementation(rootProject.libs.ktor.server.websockets)
    implementation(rootProject.libs.ktor.server.metrics.micrometer)
    implementation(rootProject.libs.ktor.server.metrics.micrometer.jvm)
    implementation(rootProject.libs.micrometer.registry.prometheus)
    implementation(rootProject.libs.kotlinx.coroutines.core)
    implementation(rootProject.libs.logback)
    implementation(rootProject.libs.webauthn4j.core)
    implementation(rootProject.libs.slf4j.api)

    testImplementation(testFixtures(project(":domain")))
    testImplementation(rootProject.libs.kotlinx.coroutines.test)
    testImplementation(rootProject.libs.ktor.server.test.host)
    testImplementation(rootProject.libs.ktor.client.content.negotiation)
    testImplementation(rootProject.libs.ktor.serialization.kotlinx.json)
    testImplementation(rootProject.libs.ktor.serialization.kotlinx.json)
}
