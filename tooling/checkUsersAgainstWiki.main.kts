@file:DependsOn("io.ktor:ktor-client-core-jvm:2.3.12")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:2.3.12")
@file:DependsOn("io.ktor:ktor-client-content-negotiation-jvm:2.3.12")
@file:DependsOn("io.ktor:ktor-client-auth-jvm:2.3.12")
@file:DependsOn("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")

@file:DependsOn("io.ktor:ktor-client-okhttp:2.3.12")
@file:DependsOn("io.ktor:ktor-client-gson:2.3.12")
@file:DependsOn("../web/build/libs/web-0.0.0-SNAPSHOT.jar")

import cloud.fabX.fabXaccess.user.rest.User
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import java.io.File
import kotlin.text.Regex
import kotlinx.coroutines.runBlocking
import cloud.fabX.fabXaccess.user.rest.CardIdentity


val fabxUsername = System.getenv("USERNAME") ?: "admin"
val fabxPassword = System.getenv("PASSWORD") ?: "password"
val fabxBaseUrl = System.getenv("BASE_URL") ?: "http://localhost:8080"

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

fun readWikiUsers(): Set<String> {
    val regex = Regex("""data-username="(?<wikiname>[\w]+)"""")

    val wikinames = File("./wikiusers.html").bufferedReader().readLines()
        .flatMap { line ->
            regex.findAll(line).map { matchResult -> matchResult.groups["wikiname"]!!.value }
        }
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
