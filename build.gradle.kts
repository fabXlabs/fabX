import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    base
    kotlin("jvm") version "2.0.20"
    jacoco
    id("com.github.ben-manes.versions") version "0.51.0"
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
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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
        implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:2.0.20"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("io.arrow-kt:arrow-core:1.2.4")
        implementation("org.kodein.di:kodein-di:7.22.0")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.0")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0")
        testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
        testImplementation("org.mockito:mockito-junit-jupiter:5.13.0")
    }
}
