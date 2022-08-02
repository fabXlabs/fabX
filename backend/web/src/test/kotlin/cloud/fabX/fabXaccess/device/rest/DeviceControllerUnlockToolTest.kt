package cloud.fabX.fabXaccess.device.rest

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.UnlockingTool
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.auth.UserPasswordCredential
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@InternalAPI
@ExperimentalSerializationApi
@MockitoSettings
class DeviceControllerUnlockToolTest {
    private lateinit var unlockingTool: UnlockingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure RestModule`(
        @Mock unlockingTool: UnlockingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.unlockingTool = unlockingTool
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { unlockingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when unlocking tool then returns http no content`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val toolId = ToolIdFixture.arbitrary()
        val requestBody = ToolUnlockDetails(toolId.serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            unlockingTool.unlockTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(toolId)
            )
        ).thenReturn(Unit.right())

        // when
        val result = handleRequest(
            HttpMethod.Post,
            "/api/v1/device/${deviceId.serialize()}/unlock-tool"
        ) {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when unlocking tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val requestBody = ToolUnlockDetails(ToolIdFixture.arbitrary().serialize())

        val message = "abc1234"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when
        val result = handleRequest(
            HttpMethod.Post,
            "/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/unlock-tool"
        ) {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isError(
                "UserNotAdmin",
                message
            )
    }

    @Test
    fun `given invalid device id when unlocking tool then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidDeviceId = "invalidDeviceId"

        val requestBody = ToolUnlockDetails(ToolIdFixture.arbitrary().serialize())

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(
            HttpMethod.Post,
            "/api/v1/device/$invalidDeviceId/unlock-tool"
        ) {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(result.response.content)
            .isNotNull()
            .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
    }

    @Test
    fun `given domain error when unlocking tool then returns mapped domain error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()

        val toolId = ToolIdFixture.arbitrary()
        val requestBody = ToolUnlockDetails(toolId.serialize())

        val correlationId = CorrelationIdFixture.arbitrary()

        val error = Error.ReferencedToolNotFound("some message", toolId, correlationId)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            unlockingTool.unlockTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(toolId)
            )
        ).thenReturn(error.left())

        // when
        val result = handleRequest(
            HttpMethod.Post,
            "/api/v1/device/${deviceId.serialize()}/unlock-tool"
        ) {
            addBasicAuth(username, password)
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(result.response.content)
            .isError(
                "ReferencedToolNotFound",
                "some message",
                mapOf("toolId" to toolId.serialize()),
                correlationId.serialize()
            )
    }
}