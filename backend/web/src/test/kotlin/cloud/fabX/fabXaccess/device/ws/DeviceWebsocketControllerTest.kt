package cloud.fabX.fabXaccess.device.ws

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.ErrorPrincipal
import io.ktor.auth.UserPasswordCredential
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readReason
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@OptIn(InternalAPI::class)
@MockitoSettings
internal class DeviceWebsocketControllerTest {
    private lateinit var commandHandler: DeviceCommandHandler
    private lateinit var authenticationService: AuthenticationService

    private val mac = "AABBCCDDEEFF"
    private val secret = "abcdef0123456789abcdef0123456789"
    private val actingDevice = DeviceFixture.withIdentity(MacSecretIdentity(mac, secret))

    @BeforeEach
    fun `configure RestModule`(
        @Mock commandHandler: DeviceCommandHandler,
        @Mock authenticationService: AuthenticationService,
    ) {
        this.commandHandler = commandHandler
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { commandHandler }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when connection establishes then sends welcome message`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
            .thenReturn(DevicePrincipal(actingDevice))

        // when & then
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, _ ->
            val greetingText = (incoming.receive() as Frame.Text).readText()
            assertThat(greetingText)
                .isEqualTo(
                    "connected to fabX"
                )
        }
    }

    @Test
    fun `when receiving command then calls command handler and returns response`() = withConfiguredTestApp {
        // given
        val commandId = 24234L
        val command = GetConfiguration(commandId)

        val expectedResponse = ConfigurationResponse(
            commandId,
            "name",
            "https://example.com/bg.bmp",
            "https://backup.example.com",
            mapOf()
        )

        whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
            .thenReturn(DevicePrincipal(actingDevice))

        whenever(commandHandler.handle(actingDevice.asActor(), command))
            .thenReturn(expectedResponse.right())

        // when & then
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, outgoing ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response).isEqualTo(expectedResponse)
        }
    }

    @Test
    fun `given error in command handler when receiving command then returns error`() = withConfiguredTestApp {
        // given
        val commandId = 24234L
        val command = GetConfiguration(commandId)

        val errorResponse = ErrorResponse(
            commandId,
            "msg",
            mapOf(),
            null
        )

        whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
            .thenReturn(DevicePrincipal(actingDevice))

        whenever(commandHandler.handle(actingDevice.asActor(), command))
            .thenReturn(errorResponse.right())

        // when & then
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, outgoing ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response).isEqualTo(errorResponse)
        }
    }

    @Test
    fun `given invalid authentication when connecting then connection is closed`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(any()))
            .thenReturn(ErrorPrincipal(Error.NotAuthenticated("msg")))

        // when & then
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth("abc", "invalid")
        }) { incoming, _ ->
            val closeReason = (incoming.receive() as Frame.Close).readReason()
            assertThat(closeReason).isEqualTo(
                CloseReason(
                    CloseReason.Codes.VIOLATED_POLICY,
                    "invalid authentication: NotAuthenticated(message=msg)"
                )
            )
        }
    }

    @Test
    fun `given no authentication when connecting then connection is closed`() = withConfiguredTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device/ws") {
            // no authentication
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }
}