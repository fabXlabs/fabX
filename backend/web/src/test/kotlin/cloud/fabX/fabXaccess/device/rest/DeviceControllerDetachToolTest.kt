package cloud.fabX.fabXaccess.device.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.DetachingTool
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeviceControllerDetachToolTest {
    private lateinit var detachingTool: DetachingTool
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock detachingTool: DetachingTool,
        @Mock authenticationService: AuthenticationService
    ) {
        this.detachingTool = detachingTool
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { detachingTool }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when detaching tool then returns http no content`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val pin = 2

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            detachingTool.detachTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(pin),
            )
        ).thenReturn(None)

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/device/${deviceId.serialize()}/attached-tool/$pin"
        ) {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given no admin authentication when detaching tool then returns http forbidden`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val pin = 2

        val message = "abc12345"
        val error = Error.UserNotAdmin(message)

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(ErrorPrincipal(error))

        // when

        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/device/${deviceId.serialize()}/attached-tool/$pin"
        ) {
            addBasicAuth(username, password)
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
    fun `given invalid device id when detaching tool then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidDeviceId = "invalidDeviceId"

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/device/$invalidDeviceId/attached-tool/32"
        ) {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(result.response.content)
            .isNotNull()
            .isEqualTo("Required UUID parameter \"id\" not given or invalid.")
    }

    @Test
    fun `given invalid pin when detaching tool then returns http bad request`() = withConfiguredTestApp {
        // given
        val invalidPin = "invalidPin"

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/device/${DeviceIdFixture.arbitrary().serialize()}/attached-tool/$invalidPin"
        ) {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(result.response.content)
            .isNotNull()
            .isEqualTo("Required int parameter \"pin\" not given or invalid.")
    }

    @Test
    fun `given domain error when detaching tool then returns mapped domain error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val pin = 2

        val correlationId = CorrelationIdFixture.arbitrary()

        val error = Error.PinNotInUse(
            "error message",
            pin,
            correlationId
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            detachingTool.detachTool(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(deviceId),
                eq(pin),
            )
        ).thenReturn(error.some())

        // when
        val result = handleRequest(
            HttpMethod.Delete,
            "/api/v1/device/${deviceId.serialize()}/attached-tool/$pin"
        ) {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isError(
                "PinNotInUse",
                "error message",
                mapOf("pin" to pin.toString()),
                correlationId.serialize()
            )
    }
}