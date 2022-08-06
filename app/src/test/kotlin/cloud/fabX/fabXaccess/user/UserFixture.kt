package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.IsAdminDetails
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.rest.QualificationAdditionDetails
import cloud.fabX.fabXaccess.user.rest.UserCreationDetails
import cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun TestApplicationEngine.givenUser(
    firstName: String = "first",
    lastName: String = "last",
    wikiName: String = "wiki"
): String {
    val requestBody = UserCreationDetails(
        firstName,
        lastName,
        wikiName
    )

    val result = handleRequest(HttpMethod.Post, "/api/v1/user") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
    return result.response.content!!
}

internal suspend fun ApplicationTestBuilder.givenUser(
    firstName: String = "first",
    lastName: String = "last",
    wikiName: String = "wiki"
): String {
    val requestBody = UserCreationDetails(
        firstName,
        lastName,
        wikiName
    )

    val response = c().post("/api/v1/user") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    return response.bodyAsText()
}

internal suspend fun ApplicationTestBuilder.givenUserIsAdmin(
    userId: String,
    isAdmin: Boolean
) {
    val requestBody = IsAdminDetails(isAdmin)

    val response = c().put("/api/v1/user/$userId/is-admin") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}

internal fun TestApplicationEngine.givenUserIsAdmin(
    userId: String,
    isAdmin: Boolean
) {
    val requestBody = IsAdminDetails(isAdmin)

    val result = handleRequest(HttpMethod.Put, "/api/v1/user/$userId/is-admin") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationTestBuilder.givenUserIsInstructorFor(
    userId: String,
    qualificationId: String
) {
    val requestBody = QualificationAdditionDetails(qualificationId)

    val response = c().post("/api/v1/user/$userId/instructor-qualification") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}

internal fun TestApplicationEngine.givenUserIsInstructorFor(
    userId: String,
    qualificationId: String
) {
    val requestBody = QualificationAdditionDetails(qualificationId)

    val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/instructor-qualification") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationTestBuilder.givenUserHasQualificationFor(
    userId: String,
    qualificationId: String
) {
    val instructorUserId = givenUser(wikiName = "instructor-$qualificationId")
    val instructorUsername = "instructor.$qualificationId".replace('-', '.')
    val instructorPassword = "instructorPassword123"
    givenUsernamePasswordIdentity(instructorUserId, instructorUsername, instructorPassword)
    givenUserIsInstructorFor(instructorUserId, qualificationId)

    val requestBody = QualificationAdditionDetails(qualificationId)

    val response = c().post("/api/v1/user/$userId/member-qualification") {
        basicAuth(instructorUsername, instructorPassword)
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}

internal fun TestApplicationEngine.givenUserHasQualificationFor(
    userId: String,
    qualificationId: String
) {
    val instructorUserId = givenUser(wikiName = "instructor-$qualificationId")
    val instructorUsername = "instructor.$qualificationId".replace('-', '.')
    val instructorPassword = "instructorPassword123"
    givenUsernamePasswordIdentity(instructorUserId, instructorUsername, instructorPassword)
    givenUserIsInstructorFor(instructorUserId, qualificationId)

    val requestBody = QualificationAdditionDetails(qualificationId)

    val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/member-qualification") {
        addBasicAuth(instructorUsername, instructorPassword)
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}

internal fun TestApplicationEngine.givenUsernamePasswordIdentity(
    userId: String,
    username: String,
    password: String
) {
    val requestBody = UsernamePasswordIdentity(username, password)

    val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/identity/username-password") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.content).isNull()
    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationTestBuilder.givenUsernamePasswordIdentity(
    userId: String,
    username: String,
    password: String
) {
    val requestBody = UsernamePasswordIdentity(username, password)

    val response = c().post("/api/v1/user/$userId/identity/username-password") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.bodyAsText()).isEmpty()
    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationTestBuilder.givenCardIdentity(
    userId: String,
    cardId: String,
    cardSecret: String
) {
    val requestBody = CardIdentity(cardId, cardSecret)

    val response = c().post("/api/v1/user/$userId/identity/card") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.bodyAsText()).isEmpty()
    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}


internal fun TestApplicationEngine.givenCardIdentity(
    userId: String,
    cardId: String,
    cardSecret: String
) {
    val requestBody = CardIdentity(cardId, cardSecret)

    val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/identity/card") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.content).isNull()
    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}

internal suspend fun ApplicationTestBuilder.givenPhoneNrIdentity(
    userId: String,
    phoneNr: String
) {
    val requestBody = PhoneNrIdentity(phoneNr)

    val response = c().post("/api/v1/user/$userId/identity/phone") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.bodyAsText()).isEmpty()
    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}

internal fun TestApplicationEngine.givenPhoneNrIdentity(
    userId: String,
    phoneNr: String
) {
    val requestBody = PhoneNrIdentity(phoneNr)

    val result = handleRequest(HttpMethod.Post, "/api/v1/user/$userId/identity/phone") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.content).isNull()
    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}