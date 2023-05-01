package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.rest.ToolUnlockDetails
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.ServerToDeviceCommand
import cloud.fabX.fabXaccess.device.ws.ToolUnlockResponse
import cloud.fabX.fabXaccess.device.ws.UnlockTool
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.async
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class UnlockToolAtDeviceViaWebsocketIntegrationTest {

    @Test
    fun `when unlocking tool then sends command, receives answer, returns http no content`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "c8760b55353aa7bfc536f2d29499b549"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolId = givenTool()
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

            // then
            assertThat(command)
                .isInstanceOf(UnlockTool::class)
                .transform { it.toolId }
                .isEqualTo(toolId)

            assertThat(httpResponse.status).isEqualTo(HttpStatusCode.NoContent)
            assertThat(httpResponse.bodyAsText()).isEmpty()
        }
    }

    @Test
    fun `given device does not respond when unlocking tool then returns http service unavailable`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "6d96ff992cc0004843a1d8a9d7118986"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolId = givenTool()
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

            // no response sent

            val httpResponse = httpResponseDeferred.await()

            // then
            assertThat(command)
                .isInstanceOf(UnlockTool::class)
                .transform { it.toolId }
                .isEqualTo(toolId)

            assertThat(httpResponse.status).isEqualTo(HttpStatusCode.ServiceUnavailable)
            assertThat(httpResponse.body<Error>())
                .isError(
                    "DeviceTimeout",
                    "Timeout while waiting for response from device DeviceId(value=$deviceId).",
                    mapOf("deviceId" to deviceId)
                )
        }
    }

    @Test
    fun `given tool not attached to device when unlocking tool then returns http unprocessable entity`() =
        withTestApp {
            // given
            val deviceId = givenDevice(mac = "AABBCCDDEEFF")
            val toolId = ToolIdFixture.arbitrary().serialize()

            // when
            val response = c().post("/api/v1/device/$deviceId/unlock-tool") {
                adminAuth()
                contentType(ContentType.Application.Json)
                setBody(ToolUnlockDetails(toolId))
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
            assertThat(response.body<Error>())
                .isError(
                    "ToolNotAttachedToDevice",
                    "Tool ToolId(value=$toolId) not attached to device DeviceId(value=$deviceId).",
                    mapOf(
                        "deviceId" to deviceId,
                        "toolId" to toolId
                    )
                )
        }
}