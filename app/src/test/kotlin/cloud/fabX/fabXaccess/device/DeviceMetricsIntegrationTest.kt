package cloud.fabX.fabXaccess.device

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import org.junit.jupiter.api.Test

class DeviceMetricsIntegrationTest {

    @Test
    fun `given no connected devices when getting device metrics then all devices disconnected`() = withTestApp {
        // given
        val deviceId1 = givenDevice(mac = "AABBCCDDEE01")
        val deviceId2 = givenDevice(mac = "AABBCCDDEE02")
        val deviceId3 = givenDevice(mac = "AABBCCDDEE03")

        // when
        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).all {
            contains("fabx_devices_connected{deviceId=\"${deviceId1}\",} 0.0")
            contains("fabx_devices_connected{deviceId=\"${deviceId2}\",} 0.0")
            contains("fabx_devices_connected{deviceId=\"${deviceId3}\",} 0.0")
        }
    }

    @Test
    fun `given one connected device when getting device metrics then one device connected`() = withTestApp {
        // given
        val mac = "AABBCCDDEE01"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceId1 = givenDevice(mac = mac, secret = secret)
        val deviceId2 = givenDevice(mac = "AABBCCDDEE02")

        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            // when
            val response = c().get("/metrics") {
                basicAuth("metrics", "supersecretmetricspassword")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).all {
                contains("fabx_devices_connected{deviceId=\"${deviceId1}\",} 1.0")
                contains("fabx_devices_connected{deviceId=\"${deviceId2}\",} 0.0")
            }
        }
    }
}