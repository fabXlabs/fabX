package cloud.fabX.fabXaccess.qualification

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder

internal suspend fun ApplicationTestBuilder.givenQualification(
    name: String = "qualification",
    description: String = "some qualification",
    colour: String = "#aabbcc",
    orderNr: Int = 42
): String {
    val requestBody = QualificationCreationDetails(name, description, colour, orderNr)

    val response = c().post("/api/v1/qualification") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    return response.bodyAsText()
}

internal suspend fun ApplicationTestBuilder.givenQualifications(count: Int): List<String> {
    return (0..count).map { givenQualification(name = "qualification $it", description = "some qualification $it") }
}