import org.gradle.api.tasks.testing.logging.TestLogEvent

val arrowVersion: String by project

plugins {
    base
    kotlin("jvm") version "1.5.20"
}

allprojects {
    group = "cloud.fabX"
    version = "2.0-SNAPSHOT"

    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "16"
        }
    }

    tasks {
        test {
            useJUnitPlatform()

            testLogging {
                events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
            }
        }
    }

    dependencies {
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        implementation(kotlin("stdlib"))
        implementation("io.arrow-kt:arrow-core:$arrowVersion")

        testImplementation(kotlin("test"))
        testImplementation(kotlin("test-junit5"))
        testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.24")
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
        testImplementation("org.mockito:mockito-junit-jupiter:3.11.2")
    }
}
