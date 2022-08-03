plugins {
    java
    id("com.github.node-gradle.node") version "3.2.1"
}

node {
    download.set(false)
    npmInstallCommand.set("ci")
}

val buildTask = tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
    inputs.dir("node_modules")
    inputs.files("angular.json", ".browserslistrc", "tsconfig.json", "tsconfig.app.json")
    outputs.dir("${project.projectDir}/dist")
}

sourceSets {
    java {
        main {
            resources {
                // This makes the processResources task automatically depend on the buildWebapp one
                srcDir(buildTask)
            }
        }
    }
}