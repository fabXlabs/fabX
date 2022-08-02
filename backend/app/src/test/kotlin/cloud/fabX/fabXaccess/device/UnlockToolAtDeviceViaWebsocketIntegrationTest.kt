package cloud.fabX.fabXaccess.device

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNullOrEmpty
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.rest.ToolUnlockDetails
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.ServerToDeviceCommand
import cloud.fabX.fabXaccess.device.ws.ToolUnlockResponse
import cloud.fabX.fabXaccess.device.ws.UnlockTool
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.coroutines.async
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class UnlockToolAtDeviceViaWebsocketIntegrationTest {

    @Test
    fun `when unlocking tool then sends command, receives answer, returns http no content`() = withTestApp {
        // given
        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolId = givenTool()
        givenToolAttachedToDevice(deviceId, 1, toolId)

        // when
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, outgoing ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResultDeferred = async {
                handleRequest(HttpMethod.Post, "/api/v1/device/$deviceId/unlock-tool") {
                    addAdminAuth()
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(ToolUnlockDetails(toolId)))
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            val response = ToolUnlockResponse(command.commandId)
            outgoing.send(Frame.Text(Json.encodeToString<DeviceResponse>(response)))

            val httpResult = httpResultDeferred.await()

            // then
            assertThat(command)
                .isInstanceOf(UnlockTool::class)
                .transform { it.toolId }
                .isEqualTo(toolId)

            assertThat(httpResult.response)
                .all {
                    transform { it.status() }.isEqualTo(HttpStatusCode.NoContent)
                    transform { it.content }.isNullOrEmpty()
                }
        }
    }

    @Test
    fun `given device does not respond when unlocking tool then returns http service unavailable`() = withTestApp {
        // given
        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolId = givenTool()
        givenToolAttachedToDevice(deviceId, 1, toolId)

        // when
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, _ ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResultDeferred = async {
                handleRequest(HttpMethod.Post, "/api/v1/device/$deviceId/unlock-tool") {
                    addAdminAuth()
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(ToolUnlockDetails(toolId)))
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            // no response sent

            val httpResult = httpResultDeferred.await()

            // then
            assertThat(command)
                .isInstanceOf(UnlockTool::class)
                .transform { it.toolId }
                .isEqualTo(toolId)

            assertThat(httpResult.response.status()).isEqualTo(HttpStatusCode.ServiceUnavailable)
            assertThat(httpResult.response.content)
                .isError(
                    "DeviceTimeout",
                    "Timeout while waiting for response from device DeviceId(value=$deviceId).",
                    mapOf("deviceId" to deviceId)
                )
        }
    }

    @Test
    fun `given tool not attached to device when unlocking tool then returns http unprocessable entity`() = withTestApp {
        // given
        val deviceId = givenDevice(mac = "aabbccddeeff")
        val toolId = ToolIdFixture.arbitrary().serialize()

        // when
        val result = handleRequest(HttpMethod.Post, "/api/v1/device/$deviceId/unlock-tool") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(ToolUnlockDetails(toolId)))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(result.response.content)
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