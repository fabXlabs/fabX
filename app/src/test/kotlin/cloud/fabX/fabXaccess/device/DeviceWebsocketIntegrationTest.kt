package cloud.fabX.fabXaccess.device

import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.rest.Device
import cloud.fabX.fabXaccess.device.ws.AuthorizedToolsResponse
import cloud.fabX.fabXaccess.device.ws.ConfigurationResponse
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.DeviceToServerCommand
import cloud.fabX.fabXaccess.device.ws.ErrorResponse
import cloud.fabX.fabXaccess.device.ws.GetAuthorizedTools
import cloud.fabX.fabXaccess.device.ws.GetConfiguration
import cloud.fabX.fabXaccess.device.ws.ToolConfigurationResponse
import cloud.fabX.fabXaccess.device.ws.ValidSecondFactorResponse
import cloud.fabX.fabXaccess.device.ws.ValidateSecondFactor
import cloud.fabX.fabXaccess.qualification.givenQualification
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolType
import cloud.fabX.fabXaccess.user.givenCardIdentity
import cloud.fabX.fabXaccess.user.givenPinIdentity
import cloud.fabX.fabXaccess.user.givenUser
import cloud.fabX.fabXaccess.user.givenUserHasQualificationFor
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import cloud.fabX.fabXaccess.user.rest.PinIdentityDetails
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceWebsocketIntegrationTest {

    @Test
    fun `given no authentication when connecting to websocket then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/device/ws")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given invalid authentication when connecting to websocket then returns http unauthorized`() = withTestApp {
        // given

        // when & then
        c().webSocket("/api/v1/device/ws", {
            basicAuth("no.body", "secret123")
        }) {
            try {
                incoming.receive()
            } catch (_: ClosedReceiveChannelException) {
            }
            assertThat(incoming.isClosedForReceive).isTrue()
        }
    }

    @Test
    fun `given valid authentication when connecting then connects`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "081b19abe23b8c36f2117d10b93063e4"
        givenDevice(mac = mac, secret = secret)

        // when & then
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            val greetingText = (incoming.receive() as Frame.Text).readText()
            assertThat(greetingText)
                .isEqualTo(
                    "connected to fabX"
                )
        }
    }

    @Test
    fun `when getting configuration then returns configuration`() = withTestApp {
        // given
        val name = "a device"
        val background = "https://example.com/bg123.bmp"
        val backupBackendUrl = "https://backup123.example.com"
        val mac = "AABB11CC22DD"
        val secret = "bc167ed6fd750441532f6002ec891b73"
        val deviceId = givenDevice(name, background, backupBackendUrl, mac, secret)

        val toolId1 = givenTool("tool1")
        givenToolAttachedToDevice(deviceId, 1, toolId1)
        val toolId2 = givenTool("tool2", ToolType.KEEP, true, 20_000, IdleState.IDLE_LOW)
        givenToolAttachedToDevice(deviceId, 2, toolId2)

        val commandId = 678
        val command = GetConfiguration(commandId, "1.42.2")

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response)
                .isEqualTo(
                    ConfigurationResponse(
                        commandId,
                        name,
                        background,
                        backupBackendUrl,
                        mapOf(
                            1 to ToolConfigurationResponse(
                                toolId1,
                                "tool1",
                                ToolType.UNLOCK,
                                false,
                                10_000,
                                IdleState.IDLE_HIGH
                            ),
                            2 to ToolConfigurationResponse(
                                toolId2,
                                "tool2",
                                ToolType.KEEP,
                                true,
                                20_000,
                                IdleState.IDLE_LOW
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun `when getting configuration then actual firmware version is updated`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "bc167ed6fd750441532f6002ec891b73"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val commandId = 678
        val command = GetConfiguration(commandId, "1.42.2")

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response.commandId).isEqualTo(commandId)
        }

        // then
        val responseGet = c().get("/api/v1/device/$deviceId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<Device>().actualFirmwareVersion).isEqualTo("1.42.2")
    }

    @Test
    fun `when getting authorized tools then returns authorized tools`() = withTestApp {
        // given
        val qualificationId1 = givenQualification()
        val qualificationId2 = givenQualification()

        val toolId1 = givenTool()
        val toolId2 = givenTool(requiredQualifications = setOf(qualificationId1))
        val toolId3 = givenTool(requiredQualifications = setOf(qualificationId2))

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val userId = givenUser()
        givenCardIdentity(userId, cardId, cardSecret)
        givenUserHasQualificationFor(userId, qualificationId1)

        val mac = "AABB11CC22DD"
        val secret = "4318db4c2e57501b0f24fd7ddcef07a4"
        val deviceId = givenDevice(mac = mac, secret = secret)
        givenToolAttachedToDevice(deviceId, 1, toolId1)
        givenToolAttachedToDevice(deviceId, 2, toolId2)
        givenToolAttachedToDevice(deviceId, 3, toolId3)

        val commandId = 6754
        val command = GetAuthorizedTools(commandId, null, CardIdentity(cardId, cardSecret))

        // when & then
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response)
                .isEqualTo(
                    AuthorizedToolsResponse(
                        commandId,
                        setOf(toolId1, toolId2)
                    )
                )
        }
    }

    @Test
    fun `when validating second factor then returns ValidSecondFactorResponse`() = withTestApp {
        // given
        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val userId = givenUser()
        givenCardIdentity(userId, cardId, cardSecret)

        val pin = "2345"
        givenPinIdentity(userId, pin)

        val mac = "AABB11CC22DD"
        val secret = "4318db4c2e57501b0f24fd7ddcef07a4"
        givenDevice(mac = mac, secret = secret)

        val commandId = 5436
        val command = ValidateSecondFactor(
            commandId,
            null,
            CardIdentity(cardId, cardSecret),
            PinIdentityDetails(pin)
        )

        // when & then
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response)
                .isEqualTo(ValidSecondFactorResponse(commandId))
        }
    }

    @Test
    fun `given invalid second factor when validating second factor then returns ErrorResponse`() = withTestApp {
        // given
        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val userId = givenUser()
        givenCardIdentity(userId, cardId, cardSecret)

        val pin = "2345"
        givenPinIdentity(userId, pin)

        val mac = "AABB11CC22DD"
        val secret = "4318db4c2e57501b0f24fd7ddcef07a4"
        givenDevice(mac = mac, secret = secret)

        val commandId = 5436
        val command = ValidateSecondFactor(
            commandId,
            null,
            CardIdentity(cardId, cardSecret),
            PinIdentityDetails("00000")
        )

        // when & then
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command)))
            val responseText = (incoming.receive() as Frame.Text).readText()
            val response = Json.decodeFromString<DeviceResponse>(responseText)

            assertThat(response)
                .isInstanceOf(ErrorResponse::class)
                .all {
                    transform { it.commandId }.isEqualTo(commandId)
                    transform { it.message }.isEqualTo("Invalid second factor provided.")
                    transform { it.parameters }.isEqualTo(mapOf())
                }
        }
    }
}