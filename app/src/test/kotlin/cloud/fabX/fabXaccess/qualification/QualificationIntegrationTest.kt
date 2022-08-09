package cloud.fabX.fabXaccess.qualification

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.memberAuth
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.qualification.rest.Qualification
import cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails
import cloud.fabX.fabXaccess.qualification.rest.QualificationDetails
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import java.util.UUID
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class QualificationIntegrationTest {

    @Test
    fun `given no authentication when get qualification then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/qualification")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get qualification then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/qualification") {
            basicAuth("no.body", "secret")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given qualification when get qualifications then returns qualifications`() = withTestApp {
        // given
        givenQualification()

        // when
        val response = c().get("/api/v1/qualification") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<Qualification>>())
            .transform { it.map { q -> q.name } }
            .containsExactlyInAnyOrder("qualification")
    }

    @Test
    fun `given multiple qualifications when get qualifications then returns qualifications`() = withTestApp {
        // given
        givenQualifications(10)

        val expectedQualificationNames = (0..10).map { "qualification $it" }.toSet()

        // when
        val response = c().get("/api/v1/qualification") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<Qualification>>())
            .transform { it.map { q -> q.name }.toSet() }
            .isEqualTo(expectedQualificationNames)
    }

    @Test
    fun `given qualification when get qualification by id then returns qualification`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        // when
        val response = c().get("/api/v1/qualification/$qualificationId") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Qualification>())
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
        val response = client.post("/api/v1/qualification") {
            header(HttpHeaders.XForwardedProto, "https")
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(incompleteRequestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.bodyAsText())
            .isJson<Error>()
            .isError(
                "kotlinx.serialization.MissingFieldException",
                "Field 'orderNr' is required for type with serial name " +
                        "'cloud.fabX.fabXaccess.qualification.rest.QualificationCreationDetails', " +
                        "but it was missing"
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
        val response = c().post("/api/v1/qualification") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given qualification when changing qualification then returns http no content`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        val requestBody = QualificationDetails(
            name = ChangeableValue("newName"),
            description = null,
            colour = ChangeableValue("#112233"),
            orderNr = ChangeableValue(100)
        )

        // when
        val response = c().put("/api/v1/qualification/${qualificationId}") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/qualification/$qualificationId") {
            memberAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<Qualification>())
            .all {
                transform { it.name }.isEqualTo("newName")
                transform { it.description }.isEqualTo("some qualification")
                transform { it.colour }.isEqualTo("#112233")
                transform { it.orderNr }.isEqualTo(100)
            }
    }

    @Test
    fun `given invalid qualification when changing qualification then returns http not found`() = withTestApp {
        // given
        val qualificationId = QualificationIdFixture.arbitrary().serialize()

        val requestBody = QualificationDetails(
            name = null,
            description = null,
            colour = null,
            orderNr = null
        )

        // when
        val response =
            c().put("/api/v1/qualification/$qualificationId") {
                adminAuth()
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isError(
                "QualificationNotFound",
                "Qualification with id QualificationId(value=$qualificationId) not found.",
                mapOf("qualificationId" to qualificationId)
            )
    }

    @Test
    fun `given qualification when deleting qualification then returns http no content`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        // when
        val response = c().delete("/api/v1/qualification/$qualificationId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given unknown qualification when deleting qualification then returns http not found`() = withTestApp {
        // given
        val qualificationId = UUID.fromString("7f635917-048c-41e2-8946-35070a20e539")

        // when
        val response = c().delete("/api/v1/qualification/$qualificationId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isError(
                "QualificationNotFound",
                "Qualification with id QualificationId(value=7f635917-048c-41e2-8946-35070a20e539) not found.",
                mapOf("qualificationId" to qualificationId.toString())
            )
    }

    @Test
    fun `given non-admin authentication when deleting qualification then returns http forbidden`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        // when
        val response = c().delete("/api/v1/qualification/$qualificationId") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<Error>())
            .isError(
                "UserNotAdmin",
                "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin."
            )
    }
}