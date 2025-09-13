import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    base
    alias(libs.plugins.kotlin.jvm)
    jacoco
    alias(libs.plugins.versions)
}

tasks.register<GradleBuild>("stage") {
    tasks = listOf("clean", "shadowJar")
}

allprojects {
    group = "cloud.fabX"
    version = System.getenv("FABX_VERSION") ?: "0.0.0-SNAPSHOT"

    apply(plugin = "kotlin")
    apply(plugin = "jacoco")

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks {
        test {
            useJUnitPlatform()

            testLogging {
                events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
                showStandardStreams = true
            }
        }
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    }
    tasks.jacocoTestReport {
        dependsOn(tasks.test) // tests are required to run before generating the report
    }

    dependencies {
        implementation(rootProject.libs.kotlin.stdlib)
        implementation(rootProject.libs.arrow.core)
        implementation(rootProject.libs.kodein)
        implementation(rootProject.libs.kotlinx.serialization.json)

        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(rootProject.libs.kotlin.test.junit5)
        testImplementation(rootProject.libs.junit.engine)
        testImplementation(rootProject.libs.junit.params)
        testImplementation(rootProject.libs.assertk.jvm)
        testImplementation(rootProject.libs.mockito.kotlin)
        testImplementation(rootProject.libs.mockito.junit5)
    }
}
