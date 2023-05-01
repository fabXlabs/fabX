package cloud.fabX.fabXaccess.tool

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.givenDevice
import cloud.fabX.fabXaccess.device.givenToolAttachedToDevice
import cloud.fabX.fabXaccess.device.rest.ToolUnlockDetails
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.DeviceToServerNotification
import cloud.fabX.fabXaccess.device.ws.ServerToDeviceCommand
import cloud.fabX.fabXaccess.device.ws.ToolUnlockResponse
import cloud.fabX.fabXaccess.device.ws.ToolUnlockedNotification
import cloud.fabX.fabXaccess.user.givenCardIdentity
import cloud.fabX.fabXaccess.user.givenUser
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class ToolMetricsIntegrationTest {
    @Test
    fun `when receives ToolUnlockedNotification then tool usage counter is increased`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "49ecad93aac0bdff2915768bd514678f"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolName = "Some Tool"
        val toolId = givenTool(name = toolName)
        givenToolAttachedToDevice(deviceId, 1, toolId)

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val userId = givenUser()
        givenCardIdentity(userId, cardId, cardSecret)

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            val notification = ToolUnlockedNotification(toolId, null, CardIdentity(cardId, cardSecret))

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerNotification>(notification)))

            delay(100)
        }

        // then
        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).contains(
            "fabx_tool_usage_count_total{toolId=\"${toolId}\",toolName=\"$toolName\",} 1.0"
        )
    }

    @Test
    fun `when tool unlocked via command then tool usage metric is increased`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "c8760b55353aa7bfc536f2d29499b549"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolName = "Some Tool"
        val toolId = givenTool(name = toolName)
        givenToolAttachedToDevice(deviceId, 1, toolId)

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResponseDeferred = async {
                c().post("/api/v1/device/$deviceId/unlock-tool") {
                    adminAuth()
                    contentType(ContentType.Application.Json)
                    setBody(ToolUnlockDetails(toolId))
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            val response = ToolUnlockResponse(command.commandId)
            outgoing.send(Frame.Text(Json.encodeToString<DeviceResponse>(response)))

            val httpResponse = httpResponseDeferred.await()
            assertThat(httpResponse.status).isEqualTo(HttpStatusCode.NoContent)
        }

        // then
        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).contains(
            "fabx_tool_usage_count_total{toolId=\"${toolId}\",toolName=\"$toolName\",} 1.0"
        )
    }
}