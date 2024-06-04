package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.DeviceRestartResponse
import cloud.fabX.fabXaccess.device.ws.ErrorResponse
import cloud.fabX.fabXaccess.device.ws.RestartDevice
import cloud.fabX.fabXaccess.device.ws.ServerToDeviceCommand
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class RestartDeviceViaWebsocketIntegrationTest {

    @Test
    fun `when restarting device then sends command, receives answer, returns http no content`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "c8760b55353aa7bfc536f2d29499b549"
        val deviceId = givenDevice(mac = mac, secret = secret)

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResponseDeferred = async {
                c().post("/api/v1/device/$deviceId/restart") {
                    adminAuth()
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            val response = DeviceRestartResponse(command.commandId)
            outgoing.send(Frame.Text(Json.encodeToString<DeviceResponse>(response)))

            val httpResponse = httpResponseDeferred.await()

            // then
            assertThat(command).isInstanceOf(RestartDevice::class)

            assertThat(httpResponse.status).isEqualTo(HttpStatusCode.NoContent)
            assertThat(httpResponse.bodyAsText()).isEmpty()
        }
    }

    @Test
    fun `given device does not respond when restarting device then returns http service unavailable`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "c8760b55353aa7bfc536f2d29499b549"
        val deviceId = givenDevice(mac = mac, secret = secret)

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResponseDeferred = async {
                c().post("/api/v1/device/$deviceId/restart") {
                    adminAuth()
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            // no response sent

            val httpResponse = httpResponseDeferred.await()

            // then
            assertThat(command).isInstanceOf(RestartDevice::class)

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
    fun `given device responds with error when restarting device returns error`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "c8760b55353aa7bfc536f2d29499b549"
        val deviceId = givenDevice(mac = mac, secret = secret)

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResponseDeferred = async {
                c().post("/api/v1/device/$deviceId/restart") {
                    adminAuth()
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            val errorMessage = "an error occurred"
            val response = ErrorResponse(
                command.commandId,
                errorMessage,
                mapOf(),
                null
            )
            outgoing.send(Frame.Text(Json.encodeToString<DeviceResponse>(response)))

            val httpResponse = httpResponseDeferred.await()

            // then
            assertThat(command).isInstanceOf(RestartDevice::class)

            assertThat(httpResponse.status).isEqualTo(HttpStatusCode.ServiceUnavailable)
            assertThat(httpResponse.bodyAsText())
                .isJson<Error>()
                .isError(
                    "UnexpectedDeviceResponse",
                    "Unexpected device response type.",
                    mapOf(
                        "deviceId" to deviceId,
                        "response" to "ErrorResponse(commandId=${command.commandId}, message=an error occurred, " +
                                "parameters={}, correlationId=null)"
                    )
                )
        }
    }
}