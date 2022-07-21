package cloud.fabX.fabXaccess.qualification

import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.AppConfiguration
import cloud.fabX.fabXaccess.RestModule
import cloud.fabX.fabXaccess.common.rest.RestError
import cloud.fabX.fabXaccess.qualification.rest.Qualification
import cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@ExperimentalSerializationApi
class QualificationIntegrationTest {

    internal fun withTestApp(
        block: TestApplicationEngine.() -> Unit
    ) {
        AppConfiguration.configure()

        runBlocking {
            withTestApplication(RestModule.moduleConfiguration) {
                block()
            }
        }
    }

    internal fun TestApplicationEngine.givenQualification(
        name: String = "qualification",
        description: String = "some qualification",
        colour: String = "#aabbcc",
        orderNr: Int = 42
    ) {
        val requestBody = QualificationCreationDetails(name, description, colour, orderNr)

        val result = handleRequest(HttpMethod.Post, "/api/v1/qualification") {
            setBody(Json.encodeToString(requestBody))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)

        // TODO return id (which requires endpoint returning id)?
    }

    internal fun TestApplicationEngine.givenQualifications(count: Int) {
        (0..count).map { givenQualification(name = "qualification $it", description = "some qualification $it") }
    }

    internal inline fun <reified T> Assert<String>.isJson() = transform { Json.decodeFromString<T>(it) }

    @Test
    fun `given qualifications when get qualifications then returns qualifications`() = withTestApp {
        // given
        givenQualification()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification")

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
        val result = handleRequest(HttpMethod.Get, "/api/v1/qualification")

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