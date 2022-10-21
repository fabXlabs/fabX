package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.IsAdminDetails
import cloud.fabX.fabXaccess.user.rest.PhoneNrIdentity
import cloud.fabX.fabXaccess.user.rest.PinIdentityAdditionDetails
import cloud.fabX.fabXaccess.user.rest.QualificationAdditionDetails
import cloud.fabX.fabXaccess.user.rest.UserCreationDetails
import cloud.fabX.fabXaccess.user.rest.UsernamePasswordIdentityAdditionDetails
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder

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
    return response.body()
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

internal suspend fun ApplicationTestBuilder.givenUsernamePasswordIdentity(
    userId: String,
    username: String,
    password: String
) {
    val requestBody = UsernamePasswordIdentityAdditionDetails(username, password)

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


internal suspend fun ApplicationTestBuilder.givenPinIdentity(
    userId: String,
    pin: String
) {
    val requestBody = PinIdentityAdditionDetails(pin)

    val response = c().post("/api/v1/user/$userId/identity/pin") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.bodyAsText()).isEmpty()
    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}