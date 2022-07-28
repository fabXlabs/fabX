package cloud.fabX.fabXaccess.tool

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.addMemberAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.qualification.givenQualification
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.Tool
import cloud.fabX.fabXaccess.tool.rest.ToolCreationDetails
import cloud.fabX.fabXaccess.tool.rest.ToolDetails
import cloud.fabX.fabXaccess.tool.rest.ToolType
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class ToolIntegrationTest {

    @Test
    fun `given no authentication when get tools then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get tools then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addBasicAuth("no.body", "s3cr3t")
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given tools when get tools then returns tools`() = withTestApp {
        // given
        givenTool("tool1")
        givenTool("tool2")
        givenTool("tool3")

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Tool>>()
            .transform { it -> it.map { it.name } }
            .containsExactlyInAnyOrder("tool1", "tool2", "tool3")
    }

    @Test
    fun `given tool when get tool by id then returns tool`() = withTestApp {
        // given
        val toolId = givenTool()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool/$toolId") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Tool>()
            .transform { it.id }
            .isEqualTo(toolId)
    }

    @Test
    fun `given qualification does not exist when adding tool then returns error`() = withTestApp {
        // given
        val invalidQualificationId = QualificationIdFixture.arbitrary().serialize()

        val requestBody = ToolCreationDetails(
            "tool",
            ToolType.UNLOCK,
            123,
            IdleState.IDLE_HIGH,
            "https://example.com/tool",
            setOf(invalidQualificationId)
        )

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/tool") {
            addAdminAuth()
            setBody(Json.encodeToString(requestBody))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "ReferencedQualificationNotFound",
                    "Qualification with id QualificationId(value=$invalidQualificationId) not found.",
                    mapOf("qualificationId" to invalidQualificationId)
                )
            )
    }

    @Test
    fun `given qualification exists when adding tool then adds tool`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        val requestBody = ToolCreationDetails(
            "tool",
            ToolType.UNLOCK,
            123,
            IdleState.IDLE_HIGH,
            "https://example.com/tool",
            setOf(qualificationId)
        )

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/tool") {
            addAdminAuth()
            setBody(Json.encodeToString(requestBody))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isNotEmpty()

        val id = result.response.content!!
        val getResult = handleRequest(HttpMethod.Get, "/api/v1/tool/$id") {
            addMemberAuth()
        }
        assertThat(getResult.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(getResult.response.content)
            .isNotNull()
            .isJson<Tool>()
            .transform { it.requiredQualifications }
            .containsExactlyInAnyOrder(qualificationId)
    }

    @Test
    fun `given non-admin authentication when adding tool then returns http forbidden`() = withTestApp {
        // given
        val requestBody = ToolCreationDetails(
            "tool",
            ToolType.UNLOCK,
            123,
            IdleState.IDLE_HIGH,
            "https://example.com/tool",
            setOf()
        )

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/tool") {
            addMemberAuth()
            setBody(Json.encodeToString(requestBody))
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given tool when changing tool then returns http no content`() = withTestApp {
        // given
        val toolId = givenTool()

        val qualificationId = givenQualification()

        val requestBody = ToolDetails(
            ChangeableValue("newName"),
            null,
            ChangeableValue(987),
            ChangeableValue(IdleState.IDLE_LOW),
            ChangeableValue(false),
            null,
            ChangeableValue(setOf(qualificationId))
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/tool/$toolId") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()

        val resultGet = handleRequest(HttpMethod.Get, "/api/v1/tool/$toolId") {
            addMemberAuth()
        }
        assertThat(resultGet.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(resultGet.response.content)
            .isNotNull()
            .isJson<Tool>()
            .isEqualTo(
                Tool(
                    toolId,
                    2,
                    "newName",
                    ToolType.UNLOCK,
                    987,
                    IdleState.IDLE_LOW,
                    false,
                    "https://example.com/tool",
                    setOf(qualificationId)
                )
            )
    }

    @Test
    fun `given invalid tool when changing tool then returns http not found`() = withTestApp {
        // given
        val invalidToolId = ToolIdFixture.arbitrary().serialize()

        val requestBody = ToolDetails(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/tool/$invalidToolId") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "ToolNotFound",
                    "Tool with id ToolId(value=$invalidToolId) not found.",
                    mapOf(
                        "toolId" to invalidToolId
                    )
                )
            )
    }

    @Test
    fun `given invalid required qualification when changing tool then returns http unprocessable entity`() =
        withTestApp {
            // given
            val toolId = givenTool()

            val invalidQualificationId = QualificationIdFixture.arbitrary().serialize()

            val requestBody = ToolDetails(
                null,
                null,
                null,
                null,
                null,
                null,
                ChangeableValue(setOf(invalidQualificationId))
            )

            // when
            val result = handleRequest(HttpMethod.Put, "/api/v1/tool/$toolId") {
                addAdminAuth()
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(Json.encodeToString(requestBody))
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(result.response.content)
                .isNotNull()
                .isJson<Error>()
                .isEqualTo(
                    Error(
                        "ReferencedQualificationNotFound",
                        "Qualification with id QualificationId(value=$invalidQualificationId) not found.",
                        mapOf("qualificationId" to invalidQualificationId)
                    )
                )
        }

    @Test
    fun `given tool when deleting tool then returns http no content`() = withTestApp {
        // given
        val toolId = givenTool()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/tool/$toolId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given unknown tool when deleting tool then returns http not found`() = withTestApp {
        // given
        val invalidToolId = ToolIdFixture.arbitrary().serialize()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/tool/$invalidToolId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "ToolNotFound",
                    "Tool with id ToolId(value=$invalidToolId) not found.",
                    mapOf("toolId" to invalidToolId)
                )
            )
    }

    @Test
    fun `given non-admin authentication when deleting tool then returns http forbidden`() = withTestApp {
        val toolId = givenTool()

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/tool/$toolId") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "UserNotAdmin",
                    "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin.",
                    mapOf()
                )
            )
    }
}