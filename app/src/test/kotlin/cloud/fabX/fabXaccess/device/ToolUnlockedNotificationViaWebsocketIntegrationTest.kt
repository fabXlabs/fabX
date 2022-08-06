package cloud.fabX.fabXaccess.device

import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestAppB
import cloud.fabX.fabXaccess.device.ws.DeviceToServerNotification
import cloud.fabX.fabXaccess.device.ws.ToolUnlockedNotification
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.user.givenCardIdentity
import cloud.fabX.fabXaccess.user.givenUser
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class ToolUnlockedNotificationViaWebsocketIntegrationTest {

    @Test
    fun `when receives ToolUnlockedNotification then logs tool unlocked`() = withTestAppB {
        // given
        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolId = givenTool()
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
    }
}