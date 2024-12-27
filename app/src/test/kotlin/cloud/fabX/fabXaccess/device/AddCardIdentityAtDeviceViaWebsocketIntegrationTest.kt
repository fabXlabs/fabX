package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.rest.AtDeviceCardCreationDetails
import cloud.fabX.fabXaccess.device.rest.ToolUnlockDetails
import cloud.fabX.fabXaccess.device.ws.CardCreationResponse
import cloud.fabX.fabXaccess.device.ws.CreateCard
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.ServerToDeviceCommand
import cloud.fabX.fabXaccess.device.ws.UnlockTool
import cloud.fabX.fabXaccess.user.givenUser
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class AddCardIdentityAtDeviceViaWebsocketIntegrationTest {
    @Test
    fun `when adding card identity at device then sends command, returns http content`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "c8760b55353aa7bfc536f2d29499b549"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val cardId = "12345671234567"

        val userId = givenUser(firstName = "some", lastName = "body")

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            val httpResponseDeferred = async {
                c().post("/api/v1/device/$deviceId/add-user-card-identity") {
                    adminAuth()
                    contentType(ContentType.Application.Json)
                    setBody(AtDeviceCardCreationDetails(userId))
                }
            }

            val commandText = (incoming.receive() as Frame.Text).readText()
            val command = Json.decodeFromString<ServerToDeviceCommand>(commandText)

            val response = CardCreationResponse(command.commandId, cardId)
            outgoing.send(Frame.Text(Json.encodeToString<DeviceResponse>(response)))

            val httpResponse = httpResponseDeferred.await()

            // then
            assertThat(command)
                .isInstanceOf(CreateCard::class)
                .transform { it.userName }
                .isEqualTo("some body")

            assertThat(httpResponse.status).isEqualTo(HttpStatusCode.NoContent)
            assertThat(httpResponse.bodyAsText()).isEmpty()
        }
    }

    @Test
    fun `given device does not respond when adding card identity at device then returns http service unavailable`() = withTestApp {
        TODO()
    }

    @Test
    fun `given invalid user id when adding card identity at device then returns http xxx`() = withTestApp {
        TODO()
    }
}