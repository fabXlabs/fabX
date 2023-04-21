package cloud.fabX.fabXaccess.tool

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.memberAuth
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
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class ToolIntegrationTest {

    @Test
    fun `given no authentication when get tools then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/tool")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get tools then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/tool") {
            basicAuth("no.body", "s3cr3t")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given tools when get tools then returns tools`() = withTestApp {
        // given
        givenTool("tool1")
        givenTool("tool2")
        givenTool("tool3")

        // when
        val response = c().get("/api/v1/tool") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<Tool>>())
            .transform { it -> it.map { it.name } }
            .containsExactlyInAnyOrder("tool1", "tool2", "tool3")
    }

    @Test
    fun `given tool when get tool by id then returns tool`() = withTestApp {
        // given
        val toolId = givenTool()

        // when
        val response = c().get("/api/v1/tool/$toolId") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Tool>())
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
            false,
            123,
            IdleState.IDLE_HIGH,
            "https://example.com/tool",
            setOf(invalidQualificationId)
        )

        // when
        val response = c().post("/api/v1/tool") {
            adminAuth()
            setBody(requestBody)
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<Error>())
            .isError(
                "ReferencedQualificationNotFound",
                "Qualification with id QualificationId(value=$invalidQualificationId) not found.",
                mapOf("qualificationId" to invalidQualificationId)
            )
    }

    @Test
    fun `given qualification exists when adding tool then adds tool`() = withTestApp {
        // given
        val qualificationId = givenQualification()

        val requestBody = ToolCreationDetails(
            "tool",
            ToolType.UNLOCK,
            true,
            123,
            IdleState.IDLE_HIGH,
            "https://example.com/tool",
            setOf(qualificationId)
        )

        // when
        val response = c().post("/api/v1/tool") {
            adminAuth()
            setBody(requestBody)
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<String>()).isNotEmpty()

        val id = response.body<String>()
        val getResponse = c().get("/api/v1/tool/$id") {
            memberAuth()
        }
        assertThat(getResponse.status).isEqualTo(HttpStatusCode.OK)
        assertThat(getResponse.body<Tool>())
            .transform { it.requiredQualifications }
            .containsExactlyInAnyOrder(qualificationId)
    }

    @Test
    fun `given non-admin authentication when adding tool then returns http forbidden`() = withTestApp {
        // given
        val requestBody = ToolCreationDetails(
            "tool",
            ToolType.UNLOCK,
            false,
            123,
            IdleState.IDLE_HIGH,
            "https://example.com/tool",
            setOf()
        )

        // when
        val response = c().post("/api/v1/tool") {
            memberAuth()
            setBody(requestBody)
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given tool when changing tool then returns http no content`() = withTestApp {
        // given
        val toolId = givenTool()

        val qualificationId = givenQualification()

        val requestBody = ToolDetails(
            ChangeableValue("newName"),
            null,
            ChangeableValue(true),
            ChangeableValue(987),
            ChangeableValue(IdleState.IDLE_LOW),
            ChangeableValue(false),
            ChangeableValue("new note"),
            null,
            ChangeableValue(setOf(qualificationId))
        )

        // when
        val response = c().put("/api/v1/tool/$toolId") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()

        val resultGet = c().get("/api/v1/tool/$toolId") {
            memberAuth()
        }
        assertThat(resultGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(resultGet.body<Tool>())
            .isEqualTo(
                Tool(
                    toolId,
                    2,
                    "newName",
                    ToolType.UNLOCK,
                    true,
                    987,
                    IdleState.IDLE_LOW,
                    false,
                    "new note",
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
            null,
            null,
            null
        )

        // when
        val response = c().put("/api/v1/tool/$invalidToolId") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isError(
                "ToolNotFound",
                "Tool with id ToolId(value=$invalidToolId) not found.",
                mapOf("toolId" to invalidToolId)
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
                null,
                null,
                ChangeableValue(setOf(invalidQualificationId))
            )

            // when
            val response = c().put("/api/v1/tool/$toolId") {
                adminAuth()
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(response.body<Error>())
                .isError(
                    "ReferencedQualificationNotFound",
                    "Qualification with id QualificationId(value=$invalidQualificationId) not found.",
                    mapOf("qualificationId" to invalidQualificationId)
                )
        }

    @Test
    fun `given tool when deleting tool then returns http no content`() = withTestApp {
        // given
        val toolId = givenTool()

        // when
        val response = c().delete("/api/v1/tool/$toolId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given unknown tool when deleting tool then returns http not found`() = withTestApp {
        // given
        val invalidToolId = ToolIdFixture.arbitrary().serialize()

        // when
        val response = c().delete("/api/v1/tool/$invalidToolId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isError(
                "ToolNotFound",
                "Tool with id ToolId(value=$invalidToolId) not found.",
                mapOf("toolId" to invalidToolId)
            )
    }

    @Test
    fun `given non-admin authentication when deleting tool then returns http forbidden`() = withTestApp {
        val toolId = givenTool()

        // when
        val response = c().delete("/api/v1/tool/$toolId") {
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