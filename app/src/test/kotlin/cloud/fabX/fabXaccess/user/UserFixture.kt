package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.IsAdminDetails
import cloud.fabX.fabXaccess.user.rest.QualificationAdditionDetails
import cloud.fabX.fabXaccess.user.rest.UserCreationDetails
import cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentity
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@InternalAPI
@ExperimentalSerializationApi
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

@InternalAPI
@ExperimentalSerializationApi
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

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
}

@InternalAPI
@ExperimentalSerializationApi
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

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
}

@InternalAPI
@ExperimentalSerializationApi
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

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
}

@InternalAPI
@ExperimentalSerializationApi
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
    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
}

@InternalAPI
@ExperimentalSerializationApi
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
    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
}