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
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@InternalAPI
@ExperimentalSerializationApi
internal fun TestApplicationEngine.givenQualification(
    name: String = "qualification",
    description: String = "some qualification",
    colour: String = "#aabbcc",
    orderNr: Int = 42
) {
    val requestBody = QualificationCreationDetails(name, description, colour, orderNr)

    val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
        addAdminAuth()
        setBody(Json.encodeToString(requestBody))
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)

    // TODO return id (which requires endpoint returning id)?
}

@InternalAPI
@ExperimentalSerializationApi
internal fun TestApplicationEngine.givenQualifications(count: Int) {
    (0..count).map { givenQualification(name = "qualification $it", description = "some qualification $it") }
}