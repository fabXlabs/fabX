plugins {
    java
    alias(libs.plugins.node)
}

node {
    // workaround for https://github.com/node-gradle/gradle-node-plugin/issues/152
    if(System.getenv().contains("FABX_NPM_COMMAND")) {
        npmCommand = System.getenv("FABX_NPM_COMMAND")
    }

    download.set(false)
    npmInstallCommand.set("ci")
}

tasks.npmInstall.configure {
    onlyIf { !file("${project.projectDir}/node_modules").exists() }
}

val buildTask = tasks.register<com.github.gradle.node.npm.task.NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    inputs.dir(project.fileTree("src"))
    inputs.dir("node_modules")
    inputs.files(
        "package.json",
        "package-lock.json",
        "components.json",
        "eslint.config.js",
        "postcss.config.js",
        "svelte.config.js",
        "tailwind.config.js",
        "tsconfig.json",
        "vite.config.js"
    )
    outputs.dir("${project.projectDir}/target")
    environment.put("BASE_DIR", "/sv")
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
