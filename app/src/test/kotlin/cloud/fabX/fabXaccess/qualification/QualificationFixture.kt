package cloud.fabX.fabXaccess.qualification

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun TestApplicationEngine.givenQualification(
    name: String = "qualification",
    description: String = "some qualification",
    colour: String = "#aabbcc",
    orderNr: Int = 42
): String {
    val requestBody = QualificationCreationDetails(name, description, colour, orderNr)

    val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
        addAdminAuth()
        setBody(Json.encodeToString(requestBody))
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
    return result.response.content!!
}

internal fun TestApplicationEngine.givenQualifications(count: Int): List<String> {
    return (0..count).map { givenQualification(name = "qualification $it", description = "some qualification $it") }
}