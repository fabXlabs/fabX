package cloud.fabX.fabXaccess.qualification

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.RestError
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.qualification.rest.Qualification
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
class QualificationIntegrationTest {

    @Test
    fun `given qualifications when get qualifications then returns qualifications`() = withTestApp {
        // given
        givenQualification()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Qualification>>()
            .transform { it.map { q -> q.name } }
            .containsExactlyInAnyOrder("qualification")
    }

    @Test
    fun `given multiple qualifications when get qualifications then returns qualifications`() = withTestApp {
        // given
        givenQualifications(10)

        val expectedQualificationNames = (0..10).map { "qualification $it" }.toSet()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Qualification>>()
            .transform { it.map { q -> q.name }.toSet() }
            .isEqualTo(expectedQualificationNames)
    }

    @Test
    fun `given incomplete body when adding qualification then returns http unprocessable entity`() = withTestApp {
        // given
        val incompleteRequestBody = "{" +
                "\"name\": \"qualification\"," +
                "\"description\": \"some qualification\"," +
                "\"colour\": \"#001122\"" +
                // orderNr missing
                "}"

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            addAdminAuth()
            setBody(incompleteRequestBody)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<RestError>()
            .isEqualTo(
                RestError(
                    "Field 'orderNr' is required for type with serial name " +
                            "'cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails', " +
                            "but it was missing"
                )
            )
    }
}