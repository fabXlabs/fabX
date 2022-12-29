import org.gradle.api.tasks.testing.logging.TestLogEvent

val arrowVersion: String by project
val kodeinVersion: String by project
val kotlinxDateTimeVersion: String by project
val kotlinxSerializationVersion: String by project

val junitVersion: String by project
val assertKVersion: String by project
val mockitoKotlinVersion: String by project
val mockitoJunitVersion: String by project

plugins {
    base
    kotlin("jvm") version "1.7.22"
    jacoco
    id("com.github.ben-manes.versions") version "0.44.0"
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
            jvmTarget = "16"
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
        implementation(enforcedPlatform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("io.arrow-kt:arrow-core:$arrowVersion")
        implementation("org.kodein.di:kodein-di:$kodeinVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
        testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertKVersion")
        testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitVersion")
    }
}
