plugins {
    java
    id("com.github.node-gradle.node") version "6.0.0"
}

node {
    download.set(false)
    npmInstallCommand.set("ci")
}

tasks.npmInstall.configure {
    onlyIf { !file("${project.projectDir}/node_modules").exists() }
}

val buildTask = tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
    inputs.dir("node_modules")
    inputs.files("angular.json", ".browserslistrc", "tsconfig.json", "tsconfig.app.json")
    outputs.dir("${project.projectDir}/dist")
    environment.put("FABX_VERSION", project.version.toString())
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
