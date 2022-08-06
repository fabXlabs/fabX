package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestAppB
import cloud.fabX.fabXaccess.device.ws.AuthorizedToolsResponse
import cloud.fabX.fabXaccess.device.ws.ConfigurationResponse
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.DeviceToServerCommand
import cloud.fabX.fabXaccess.device.ws.GetAuthorizedTools
import cloud.fabX.fabXaccess.device.ws.GetConfiguration
import cloud.fabX.fabXaccess.device.ws.ToolConfigurationResponse
import cloud.fabX.fabXaccess.qualification.givenQualification
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolType
import cloud.fabX.fabXaccess.user.givenCardIdentity
import cloud.fabX.fabXaccess.user.givenUser
import cloud.fabX.fabXaccess.user.givenUserHasQualificationFor
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DeviceWebsocketIntegrationTest {

    @Test
    fun `given no authentication when connecting to websocket then returns http unauthorized`() = withTestAppB {
        // given

        // when
        val response = c().get("/api/v1/device/ws")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    // TODO rewrite test to work with new test api
    @Disabled
    @Test
    fun `given invalid authentication when connecting to websocket then returns http unauthorized`() = withTestAppB {
        // given

        // when & then
        c().webSocket("/api/v1/device/ws", {
            basicAuth("no.body", "secret123")
        }) {
            val closingFrame = (incoming.receive() as Frame.Close)

            assertThat(closingFrame)
                .transform { it.readReason() }
                .isEqualTo(
                    CloseReason(
                        CloseReason.Codes.VIOLATED_POLICY,
                        "invalid authentication: UserNotFoundByIdentity(message=Not able to find user for given identity.)"
                    )
                )
        }
    }

    @Test
    fun `given valid authentication when connecting then connects`() = withTestAppB {
        // given
        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
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
    fun `when getting configuration then returns configuration`() = withTestAppB {
        // given
        val name = "a device"
        val background = "https://example.com/bg123.bmp"
        val backupBackendUrl = "https://backup123.example.com"
        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
        val deviceId = givenDevice(name, background, backupBackendUrl, mac, secret)

        val toolId1 = givenTool("tool1")
        givenToolAttachedToDevice(deviceId, 1, toolId1)
        val toolId2 = givenTool("tool2", ToolType.KEEP, 20_000, IdleState.IDLE_LOW)
        givenToolAttachedToDevice(deviceId, 2, toolId2)

        val commandId = 678L
        val command = GetConfiguration(commandId)

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
                                "tool1",
                                ToolType.UNLOCK,
                                10_000,
                                IdleState.IDLE_HIGH
                            ),
                            2 to ToolConfigurationResponse(
                                "tool2",
                                ToolType.KEEP,
                                20_000,
                                IdleState.IDLE_LOW
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun `when getting authorized tools then returns authorized tools`() = withTestAppB {
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

        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
        val deviceId = givenDevice(mac = mac, secret = secret)
        givenToolAttachedToDevice(deviceId, 1, toolId1)
        givenToolAttachedToDevice(deviceId, 2, toolId2)
        givenToolAttachedToDevice(deviceId, 3, toolId3)

        val commandId = 6754L
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
}