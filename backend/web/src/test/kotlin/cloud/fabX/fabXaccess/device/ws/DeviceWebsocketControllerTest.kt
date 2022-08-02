package cloud.fabX.fabXaccess.device.ws

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.addBasicAuth
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.model.MacSecretIdentity
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
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
import isLeft
import isRight
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.kodein.di.instance
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

    private lateinit var testee: DeviceWebsocketController

    @BeforeEach
    fun `configure RestModule`(
        @Mock commandHandler: DeviceCommandHandler,
        @Mock authenticationService: AuthenticationService,
    ) {
        this.commandHandler = commandHandler
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend TestApplicationEngine.() -> Unit) =
        withTestApp(
            {
                bindInstance(overrides = true) { commandHandler }
                bindInstance(overrides = true) { authenticationService }
            }, {
                val controller: DeviceWebsocketController by it.instance()
                testee = controller
            },
            block
        )

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

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
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

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
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

    @Test
    fun `when sending command then command is sent to connected device`() = withConfiguredTestApp {
        // given
        val command = UnlockTool(123L, ToolIdFixture.arbitrary().serialize())

        val correlationId = CorrelationIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
            .thenReturn(DevicePrincipal(actingDevice))

        // when & then
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, _ ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            val result = testee.sendCommand(actingDevice.id, command, correlationId)

            assertThat(result)
                .isRight()
                .isEqualTo(Unit)

            val incomingCommand = (incoming.receive() as Frame.Text).readText()

            assertThat(incomingCommand)
                .transform { Json.decodeFromString<ServerToDeviceCommand>(it) }
                .isEqualTo(command)
        }
    }

    @Test
    fun `given device is not connected when sending command then returns error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val command = UnlockTool(123L, ToolIdFixture.arbitrary().serialize())
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = testee.sendCommand(deviceId, command, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DeviceNotConnected(
                    "Device with id $deviceId is currently not connected.",
                    deviceId,
                    correlationId
                )
            )
    }

    @Test
    fun `when getting device response then returns device response`() = withConfiguredTestApp {
        // given
        val commandId = 567L
        val response = ToolUnlockResponse(commandId)

        val correlationId = CorrelationIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
            .thenReturn(DevicePrincipal(actingDevice))

        // when & then
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, outgoing ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            val jobGet = async {
                testee.getDeviceResponse(actingDevice.id, commandId, correlationId)
            }
            launch {
                outgoing.send(Frame.Text(Json.encodeToString<DeviceResponse>(response)))
            }

            val result = jobGet.await()
            println("result: $result")
            assertThat(result)
                .isRight()
                .isEqualTo(response)
        }
    }

    @Test
    fun `given device is not connected when getting device response then returns error`() = withConfiguredTestApp {
        // given
        val deviceId = DeviceIdFixture.arbitrary()
        val commandId = 987L
        val correlationId = CorrelationIdFixture.arbitrary()

        // when
        val result = testee.getDeviceResponse(deviceId, commandId, correlationId)

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(
                Error.DeviceNotConnected(
                    "Device with id $deviceId is currently not connected.",
                    deviceId,
                    correlationId
                )
            )
    }

    // TODO with ktor 2 refactor test for virtual time
    @Test
    fun `given device does not respond when getting device response then returns error after timeout`() =
        withConfiguredTestApp {
            // given
            val commandId = 567L
            val deviceId = actingDevice.id

            val correlationId = CorrelationIdFixture.arbitrary()

            whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
                .thenReturn(DevicePrincipal(actingDevice))

            // when & then
            handleWebSocketConversation("/api/v1/device/ws", {
                addBasicAuth(mac, secret)
            }) { incoming, _ ->
                (incoming.receive() as Frame.Text).readText() // greeting text

                val timeBefore = System.currentTimeMillis()
                val result = testee.getDeviceResponse(deviceId, commandId, correlationId)
                val deltaTime = System.currentTimeMillis() - timeBefore

                assertThat(result)
                    .isLeft()
                    .isEqualTo(
                        Error.DeviceTimeout(
                            "Timeout while waiting for response from device $deviceId.",
                            deviceId,
                            correlationId
                        )
                    )
                assertThat(deltaTime)
                    .isGreaterThanOrEqualTo(5000)
            }
        }
}