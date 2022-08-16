package cloud.fabX.fabXaccess.device.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.AttachingTool
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeviceControllerAttachToolTest {
    private lateinit var attachingTool: AttachingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock attachingTool: AttachingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.attachingTool = attachingTool
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { attachingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when attaching tool then returns http no content`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val pin = 3

        val toolId = ToolIdFixture.arbitrary()
        val requestBody = ToolAttachmentDetails(toolId.serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            attachingTool.attachTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(pin),
                eq(toolId)
            )
        ).thenReturn(None)

        // when
        val response = c().put("/api/v1/device/${deviceId.serialize()}/attached-tool/$pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when attaching tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = ToolAttachmentDetails(ToolIdFixture.arbitrary().serialize())

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val response = c().put("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-tool/42") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "UserNotAdmin",
                message
            )
    }

    @Test
    fun `given no body when attaching tool then returns http unprocessable entity`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-tool/123") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            // empty body
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
    }

    @Test
    fun `given invalid device id when attaching tool then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidDeviceId = "invalidDeviceId"

        val requestBody = ToolAttachmentDetails(ToolIdFixture.arbitrary().serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/device/$invalidDeviceId/attached-tool/123") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(response.body<String>())
            .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
    }

    @Test
    fun `given invalid pin when attaching tool then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidPin = "invalidPin"

        val requestBody = ToolAttachmentDetails(ToolIdFixture.arbitrary().serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val response = c().put("/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-tool/$invalidPin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(response.body<String>())
            .isEqualTo("Required int parameter \"pin\" not given or invalid.")
    }

    @Test
    fun `given domain error when attaching tool then returns mapped domain error`() = withConfiguredTestApp {
        val deviceId = DeviceIdFixture.arbitrary()
        val pin = 3

        val toolId = ToolIdFixture.arbitrary()
        val requestBody = ToolAttachmentDetails(toolId.serialize())

        val correlationId = CorrelationIdFixture.arbitrary()

        val error = Error.ReferencedToolNotFound(
            "some message",
            toolId,
            correlationId
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            attachingTool.attachTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(pin),
                eq(toolId)
            )
        ).thenReturn(error.some())

        // when
        val response = c().put("/api/v1/device/${deviceId.serialize()}/attached-tool/$pin") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "ReferencedToolNotFound",
                "some message",
                mapOf("toolId" to toolId.serialize()),
                correlationId.serialize()
            )
    }
}