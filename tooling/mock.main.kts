@file:DependsOn("io.ktor:ktor-client-core-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-content-negotiation-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-auth-jvm:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-serialization-kotlinx-json-jvm:2.0.0-beta-1")

@file:DependsOn("io.ktor:ktor-client-okhttp:2.0.0-beta-1")
@file:DependsOn("io.ktor:ktor-client-gson:2.0.0-beta-1")

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val client = HttpClient(CIO) {
    install(Auth) {
        basic {
            credentials {
                BasicAuthCredentials(username = "admin", password = "password")
            }
        }
    }
}

val adminUserId = runBlocking {
    val response = client.get("http://localhost:8080/api/v1/user")
    Json.parseToJsonElement(response.bodyAsText()).jsonArray[0].jsonObject["id"]!!.jsonPrimitive.content
}

// qualifications
val qualifications = listOf("AM Laser", "Door Shop", "OHWL", "Screw", "Saw&Sand", "Router", "Shaper", "Lathe")
val qualificationIds = mutableListOf<String>()

// devices
val devices = listOf("Station", "Kappstation", "Shaper", "Door Shop", "Door OHWL", "AM Laser")
val deviceIds = mutableListOf<String>()

// tools
val tools =
    listOf(
        "OFK 500",
        "DF 500",
        "TS 55",
        "ETSC 125",
        "T 18",
        "TXS",
        "Kappstation",
        "Shaper",
        "Door Shop",
        "Door OHWL",
        "AM Laser"
    )
val toolQualification =
    listOf(
        "Router",
        "Router",
        "Saw&Sand",
        "Saw&Sand",
        "Screw",
        "Screw",
        "Saw&Sand",
        "Shaper",
        "Door Shop",
        "OHWL",
        "AM Laser"
    )
val toolIds = mutableListOf<String>()

val toolDevices =
    listOf(
        "Station",
        "Station",
        "Station",
        "Station",
        "Station",
        "Station",
        "Kappstation",
        "Shaper",
        "Door Shop",
        "Door OHWL",
        "AM Laser"
    )
val toolPins = listOf(1, 2, 3, 4, 5, 6, 1, 7, 1, 1, 1)

val users = listOf(
    "Parisa Tabriz",
    "Roberto Tamassia",
    "Andrew S. Tanenbaum",
    "Austin Tate",
    "Bernhard Thalheim",
    "Éva Tardos",
    "Gábor Tardos",
    "Robert Tarjan",
    "Valerie Taylor",
    "Mario Tchou",
    "Jaime Teevan",
    "Shang-Hua Teng",
    "Larry Tesler",
    "Avie Tevanian",
    "Charles P. Thacker",
    "Daniel Thalmann",
    "Ken Thompson",
    "Sebastian Thrun",
    "Walter F. Tichy",
    "Seinosuke Toda",
    "Linus Torvalds",
    "Leonardo Torres",
    "Godfried Toussaint",
    "Gloria Townsend",
    "Edwin E. Tozer",
    "Joseph F Traub",
    "John V. Tucker",
    "John Tukey",
    "Alan Turing",
    "David Turner",
    "Murray Turoff"
)
val userIds = mutableListOf<String>()

qualifications.forEach { qualificationName ->
    val body = "{" +
            "\"name\": \"${qualificationName}\", " +
            "\"description\": \"${qualificationName} description\", " +
            "\"colour\": \"#000000\", " +
            "\"orderNr\": 100 " +
            "}"

    runBlocking {
        val response = client.post("http://localhost:8080/api/v1/qualification") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        qualificationIds.add(response.bodyAsText())
    }
}

qualificationIds.forEach { qualificationId ->
    val body = "{\"qualificationId\": \"$qualificationId\"}"

    runBlocking {
        client.post("http://localhost:8080/api/v1/user/$adminUserId/instructor-qualification") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

devices.forEachIndexed { i, deviceName ->
    val body = "{" +
            "\"name\": \"${deviceName}\", " +
            "\"background\": \"https://example.com/bg1.bmp\", " +
            "\"backupBackendUrl\": \"https://backup.example.com\", " +
            "\"mac\": \"aabbcc${i.toString(16).padStart(6, '0')}\" " +
            "\"secret\": \"supersecret\" " +
            "}"

    runBlocking {
        val response = client.post("http://localhost:8080/api/v1/device") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        deviceIds.add(response.bodyAsText())
    }
}

tools.forEachIndexed { i, toolName ->
    val qualificationId = qualificationIds[qualifications.indexOf(toolQualification[i])]

    val body = "{" +
            "\"name\": \"${toolName}\", " +
            "\"type\": \"UNLOCK\", " +
            "\"time\": 300, " +
            "\"idleState\": \"IDLE_LOW\" " +
            "\"wikiLink\": \"https://wiki.example.com\" " +
            "\"requiredQualifications\": [\"$qualificationId\"]" +
            "}"

    runBlocking {
        val response = client.post("http://localhost:8080/api/v1/tool") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        toolIds.add(response.bodyAsText())
    }
}

toolDevices.forEachIndexed { i, device ->
    val toolId = toolIds[i]
    val deviceId = deviceIds[devices.indexOf(device)]
    val pin = toolPins[i]

    val body = "{ \"toolId\": \"$toolId\"}"

    runBlocking {
        client.put("http://localhost:8080/api/v1/device/$deviceId/attached-tool/$pin") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}

users.forEach { userName ->
    val lastName = userName.split(" ").last()
    val firstName = userName.substring(0, userName.length - lastName.length - 1)
    val wikiName = firstName.split(" ")[0].lowercase() + "." + lastName.lowercase()

    val body = "{ " +
            "\"firstName\": \"$firstName\", " +
            "\"lastName\": \"$lastName\", " +
            "\"wikiName\": \"$wikiName\"" +
            " }"

    runBlocking {
        val response = client.post("http://localhost:8080/api/v1/user") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        userIds.add(response.bodyAsText())
    }
}

userIds.forEach { userId ->
    qualificationIds.forEach { qualificationId ->
        if (Random.nextBoolean()) {
            val body = "{\"qualificationId\": \"$qualificationId\"}"

            runBlocking {
                client.post("http://localhost:8080/api/v1/user/$userId/member-qualification") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        }
        if (Random.nextDouble() < 0.1) {
            val body = "{\"qualificationId\": \"$qualificationId\"}"

            runBlocking {
                client.post("http://localhost:8080/api/v1/user/$userId/instructor-qualification") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            }
        }
    }
}