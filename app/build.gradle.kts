plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass.set("cloud.fabX.fabXaccess.AppKt")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "cloud.fabX.fabXaccess.AppKt"))
        }
    }
}

dependencies {
    implementation(project(":web"))
    implementation(project(":frontend-angular"))
    implementation(project(":domain"))
    implementation(project(":persistence"))
    implementation(project(":logging"))
    implementation(rootProject.libs.kotlinx.coroutines.core)
    implementation(rootProject.libs.kotlinx.datetime)

    testImplementation(testFixtures(project(":domain")))
    testImplementation(rootProject.libs.ktor.server.test.host)
    testImplementation(rootProject.libs.ktor.client.content.negotiation)
    testImplementation(rootProject.libs.ktor.serialization.kotlinx.json)
    testImplementation(rootProject.libs.wiremock)
    testImplementation(rootProject.libs.kotlinx.coroutines.test)
    testImplementation(rootProject.libs.testcontainers)
    testImplementation(rootProject.libs.testcontainers.postgres)
    testImplementation(rootProject.libs.exposed.core)
    testImplementation(rootProject.libs.hikaricp)
}
