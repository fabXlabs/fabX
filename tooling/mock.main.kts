@file:DependsOn("io.ktor:ktor-client-core-jvm:2.3.5")
@file:DependsOn("io.ktor:ktor-client-cio-jvm:2.3.5")
@file:DependsOn("io.ktor:ktor-client-content-negotiation-jvm:2.3.5")
@file:DependsOn("io.ktor:ktor-client-auth-jvm:2.3.5")
@file:DependsOn("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.5")

@file:DependsOn("io.ktor:ktor-client-okhttp:2.3.5")
@file:DependsOn("io.ktor:ktor-client-gson:2.3.5")

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
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.date.getTimeMillis
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val baseUrl = "http://localhost:8080/api/v1"

val client = HttpClient(CIO) {
    install(Auth) {
        basic {
            credentials {
                BasicAuthCredentials(username = "admin", password = "password")
            }
            sendWithoutRequest { _ -> true }
        }
    }
}

val adminUserId = runBlocking {
    val response = client.get("$baseUrl/user/me")
    assert(response.status == HttpStatusCode.OK)
    Json.parseToJsonElement(response.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content
}

// qualifications
val qualifications = listOf("AM Laser", "Door Shop", "OHWL", "Screw", "Saw&Sand", "Router", "Shaper", "Lathe")
val qualificationColours =
    listOf("#ff2600", "#ff40ff", "#ff007f", "#C49A8E", "#A15F53", "#7F2424", "#333333", "#000000")
val qualificationOrderNrs = listOf(42, 50, 51, 100, 101, 102, 106, 107)
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
val toolRequires2FAs = listOf(
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    false,
    true,
    false,
    false
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

println("Talking to backend...")
val qualificationsStartTimestamp = getTimeMillis()
qualifications.forEachIndexed { i, qualificationName ->
    val body = "{" +
            "\"name\": \"$qualificationName\", " +
            "\"description\": \"$qualificationName description\", " +
            "\"colour\": \"${qualificationColours[i]}\", " +
            "\"orderNr\": ${qualificationOrderNrs[i]} " +
            "}"

    runBlocking {
        val response = client.post("$baseUrl/qualification") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assert(response.status == HttpStatusCode.Created)
        qualificationIds.add(
            Json.parseToJsonElement(response.bodyAsText())
                .jsonPrimitive
                .contentOrNull!!
        )
    }
}
val qualificationsDelay = getTimeMillis() - qualificationsStartTimestamp
println("Took $qualificationsDelay ms to create ${qualifications.size} Qualifications.")


val makeAdminInstructorStartTimestamp = getTimeMillis()
qualificationIds.forEach { qualificationId ->
    val body = "{\"qualificationId\": \"$qualificationId\"}"

    runBlocking {
        val response = client.post("$baseUrl/user/$adminUserId/instructor-qualification") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assert(response.status == HttpStatusCode.OK)
    }
}
val makeAdminInstructorDelay = getTimeMillis() - makeAdminInstructorStartTimestamp
println("Took $makeAdminInstructorDelay ms to make admin Instructor of all ${qualificationIds.size} Qualifications.")


val deviceStartTimestamp = getTimeMillis()
devices.forEachIndexed { i, deviceName ->
    val body = "{" +
            "\"name\": \"$deviceName\", " +
            "\"background\": \"https://example.com/bg1.bmp\", " +
            "\"backupBackendUrl\": \"https://backup.example.com\", " +
            "\"mac\": \"AABBCC${i.toString(16).padStart(6, '0')}\" " +
            "\"secret\": \"a2a50c75e271104dcbaeda71c2e9a7fc\" " +
            "}"

    runBlocking {
        val response = client.post("$baseUrl/device") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assert(response.status == HttpStatusCode.Created)
        deviceIds.add(
            Json.parseToJsonElement(response.bodyAsText())
                .jsonPrimitive
                .contentOrNull!!
        )
    }
}
val devicesDelay = getTimeMillis() - deviceStartTimestamp
println("Took $devicesDelay ms to create ${devices.size} Devices.")


val toolStartTimestamp = getTimeMillis()
tools.forEachIndexed { i, toolName ->
    val qualificationId = qualificationIds[qualifications.indexOf(toolQualification[i])]
    val requires2FA = toolRequires2FAs[i]

    val body = "{" +
            "\"name\": \"$toolName\", " +
            "\"type\": \"UNLOCK\", " +
            "\"requires2FA\": $requires2FA, " +
            "\"time\": 300, " +
            "\"idleState\": \"IDLE_LOW\" " +
            "\"wikiLink\": \"https://wiki.example.com\" " +
            "\"requiredQualifications\": [\"$qualificationId\"]" +
            "}"

    runBlocking {
        val response = client.post("$baseUrl/tool") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assert(response.status == HttpStatusCode.Created)
        toolIds.add(
            Json.parseToJsonElement(response.bodyAsText())
                .jsonPrimitive
                .contentOrNull!!
        )
    }
}
val toolDelay = getTimeMillis() - toolStartTimestamp
println("Took $toolDelay ms to create ${tools.size} Tools.")


val toolAttachmentStartTimestamp = getTimeMillis()
toolDevices.forEachIndexed { i, device ->
    val toolId = toolIds[i]
    val deviceId = deviceIds[devices.indexOf(device)]
    val pin = toolPins[i]

    val body = "{ \"toolId\": \"$toolId\"}"

    runBlocking {
        val response = client.put("$baseUrl/device/$deviceId/attached-tool/$pin") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assert(response.status == HttpStatusCode.OK)
    }
}
val toolAttachmentDelay = getTimeMillis() - toolAttachmentStartTimestamp
println("Took $toolAttachmentDelay ms to attach ${toolDevices.size} Tools to Devices.")


val userStartTimestamp = getTimeMillis()
val moreUsers = users + (1..100).map { "U$it Last" }
moreUsers.parallelStream().forEach { userName ->
    val lastName = userName.split(" ").last()
    val firstName = userName.substring(0, userName.length - lastName.length - 1)
    val wikiName = firstName.split(" ")[0].lowercase() + "." + lastName.lowercase()

    val body = "{ " +
            "\"firstName\": \"$firstName\", " +
            "\"lastName\": \"$lastName\", " +
            "\"wikiName\": \"$wikiName\"" +
            " }"

    runBlocking {
        val response = client.post("$baseUrl/user") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assert(response.status == HttpStatusCode.Created)
        userIds.add(
            Json.parseToJsonElement(response.bodyAsText())
                .jsonPrimitive
                .contentOrNull!!
        )
    }
}
val usersDelay = getTimeMillis() - userStartTimestamp
println("Took $usersDelay ms to create ${moreUsers.size} Users.")


val userQualificationStartTimestamp = getTimeMillis()
var qualificationCount = 0
userIds.parallelStream().forEach { userId ->
    qualificationIds.forEach { qualificationId ->
        if (Random.nextBoolean()) {
            qualificationCount++
            val body = "{\"qualificationId\": \"$qualificationId\"}"

            runBlocking {
                val response = client.post("$baseUrl/user/$userId/member-qualification") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                assert(response.status == HttpStatusCode.OK)
            }
        }
        if (Random.nextDouble() < 0.1) {
            qualificationCount++
            val body = "{\"qualificationId\": \"$qualificationId\"}"

            runBlocking {
                val response = client.post("$baseUrl/user/$userId/instructor-qualification") {
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
                assert(response.status == HttpStatusCode.OK)
            }
        }
    }
}
val userQualificationsDelay = getTimeMillis() - userQualificationStartTimestamp
println("Took $userQualificationsDelay ms to add $qualificationCount Qualifications to Users.")

val totalDelay = getTimeMillis() - qualificationsStartTimestamp
println("Took $totalDelay ms in total.")
