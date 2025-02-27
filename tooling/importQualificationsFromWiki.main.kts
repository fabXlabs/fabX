#!/usr/bin/env kotlin

@file:DependsOn("io.ktor:ktor-client-core-jvm:3.1.0")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:3.1.0")
@file:DependsOn("io.ktor:ktor-client-content-negotiation-jvm:3.1.0")
@file:DependsOn("io.ktor:ktor-client-auth-jvm:3.1.0")
@file:DependsOn("io.ktor:ktor-serialization-kotlinx-json-jvm:3.1.0")

@file:DependsOn("io.ktor:ktor-client-okhttp:3.1.0")
@file:DependsOn("io.ktor:ktor-client-gson:3.1.0")
@file:DependsOn("../web/build/libs/web-0.0.0-SNAPSHOT.jar")

import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.QualificationAdditionDetails
import cloud.fabX.fabXaccess.user.rest.User
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import java.io.File
import kotlinx.coroutines.runBlocking

val fabxUsername = System.getenv("USERNAME") ?: "admin"
val fabxPassword = System.getenv("PASSWORD") ?: "password"
val fabxBaseUrl = System.getenv("BASE_URL") ?: "http://localhost:8080"

val QUALIFICATION_ID = "7442f60c-869f-414f-a506-96cd0f4f2dd0"

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    install(Auth) {
        basic {
            credentials {
                BasicAuthCredentials(username = fabxUsername, password = fabxPassword)
            }
        }
    }
}

fun getFabxUsersFromBackend(): List<User> {
    val fabxUsers = runBlocking {
        println("getting fabX users...")
        val r: HttpResponse = client.get("$fabxBaseUrl/api/v1/user")
        println("\t...${r.status}")
        if (r.status != HttpStatusCode.OK) {
            System.exit(42)
        }
        r.body<List<User>>()
    }

    return fabxUsers
}

fun addQualificationToUser(
    userId: String,
    qualificationId: String
) = runBlocking {
    val requestBody = QualificationAdditionDetails(qualificationId)

    val response = client.post("$fabxBaseUrl/api/v1/user/$userId/member-qualification") {
        basicAuth(fabxUsername, fabxPassword)
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    if (response.status != HttpStatusCode.NoContent) {
        System.exit(43)
    }
}

fun readWikiUsers(): Set<String> {
    val wikinames = File("./wikiSpeedyUsers.csv").bufferedReader().readLines()
        .map { line -> line.trim() }
        .distinct()
        .toSet()

    println("Found ${wikinames.size} wikinames in wiki table")
    return wikinames
}

val fabxUsers: List<User> = getFabxUsersFromBackend()
val wikinames: Set<String> = readWikiUsers()

val fabxWikiNames: Set<String> = fabxUsers.map { it.wikiName }.toSet()

// WITHOUT FABX
val usersWithoutFabx: Set<String> = wikinames - fabxWikiNames

println()
println("USERS WITHOUT fabX ACCOUNT (${usersWithoutFabx.size})")
usersWithoutFabx.sorted().forEach { println(it) }

// WITH FABX
val usersWithFabx: Set<User> = fabxUsers
    .filter { u -> wikinames.contains(u.wikiName) }
    .toSet()

// WITH FABX BUT WITHOUT CARD
val usersWithoutCard: Set<String> = usersWithFabx
    .filter { user ->
        !user.identities.any { it is CardIdentity }
    }
    .map { user -> user.wikiName }
    .toSet()

println()
println("USERS WITHOUT fabX CARD (${usersWithoutCard.size})")
usersWithoutCard.sorted().forEach { println(it) }

// WITH FABX AND WITH CARD
val usersWithCard: Set<String> = usersWithFabx
    .filter { user ->
        user.identities.any { it is CardIdentity }
    }
    .map { user -> user.wikiName }
    .toSet()

println()
println("USERS WITH fabX CARD (${usersWithCard.size})")
usersWithCard.sorted().forEach { println(it) }


// CHECK QUALIFICATION
val usersWithoutQualification: Set<String> = usersWithFabx
    .filter { user ->
        !user.memberQualifications.contains(QUALIFICATION_ID)
    }
    .map { user -> user.wikiName }
    .toSet()

println()
println("USERS WITHOUT Qualification (${usersWithoutQualification.size})")
usersWithoutQualification.sorted().forEach { println(it) }

// ADD QUALIFICATION

println("ADDING QUALIFICATION to all users without qualification...")
usersWithFabx
    .filter { user ->
        !user.memberQualifications.contains(QUALIFICATION_ID)
    }
    .forEach { user ->
        addQualificationToUser(user.id, QUALIFICATION_ID)
    }
