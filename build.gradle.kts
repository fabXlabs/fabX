import org.gradle.api.tasks.testing.logging.TestLogEvent

val arrowVersion: String by project
val junitVersion: String by project
val assertKVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunitVersion: String by project

plugins {
    base
    kotlin("jvm") version "1.5.30"
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
        implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.5.30"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("io.arrow-kt:arrow-core:$arrowVersion")

        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
        testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitVersion")
    }
}
