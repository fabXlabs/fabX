package cloud.fabX.fabXaccess.qualification

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.addMemberAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.RestError
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.qualification.rest.Qualification
import cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import java.util.UUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
class QualificationIntegrationTest {

    @Test
    fun `given no authentication when get qualification then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get qualification then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification") {
            addBasicAuth("no.body", "secret")
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given qualification when get qualifications then returns qualifications`() = withTestApp {
        // given
        givenQualification()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification") {
            addMemberAuth()
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
            addMemberAuth()
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
    fun `given qualification when get qualification by id then returns qualification`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification/$qualificationId") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Qualification>()
            .transform { it.name }
            .isEqualTo("qualification")
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

    @Test
    fun `given non-admin authentication when adding qualification then returns http forbidden`() = withTestApp {
        // given
        val requestBody = QualificationCreationDetails(
            "qualification",
            "some qualification",
            "#aabbcc",
            42
        )

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            addMemberAuth()
            setBody(Json.encodeToString(requestBody))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given qualification when deleting qualification then returns http ok`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/qualification/$qualificationId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given unknown qualification when deleting qualification then returns http not found`() = withTestApp {
        // given
        val qualificationId = UUID.fromString("7f635917-048c-41e2-8946-35070a20e539")

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/qualification/$qualificationId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<RestError>()
            .isEqualTo(
                RestError(
                    "Qualification with id QualificationId(value=7f635917-048c-41e2-8946-35070a20e539) not found.",
                    mapOf(
                        "qualificationId" to qualificationId.toString()
                    )
                )
            )
    }

    @Test
    fun `given non-admin authentication when deleting qualification then returns http forbidden`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/qualification/$qualificationId") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<RestError>()
            .isEqualTo(
                RestError(
                    "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin.",
                )
            )
    }
}