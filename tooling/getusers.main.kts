@file:DependsOn("io.ktor:ktor-client-core-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-content-negotiation-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-auth-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-serialization-kotlinx-json-jvm:2.0.0-beta-1")

@file:DependsOn("io.ktor:ktor-client-okhttp:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-gson:2.0.0-beta-1")

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ContentNegotiation
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking

val client = HttpClient(CIO) {
    install(ContentNegotiation)
    install(Auth) {
        basic {
            credentials {
                BasicAuthCredentials(username = "admin", password = "password")
            }
        }
    }
}

1.rangeTo(10)
    .map {
        val timeBefore = System.currentTimeMillis()
        runBlocking { client.get("http://localhost:8080/api/v1/user") }
        System.currentTimeMillis() - timeBefore
    }
    .forEach { println(it) }
