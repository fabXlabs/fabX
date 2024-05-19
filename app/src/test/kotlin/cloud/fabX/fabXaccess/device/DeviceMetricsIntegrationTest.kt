package cloud.fabX.fabXaccess.device

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.ws.DeviceResponse
import cloud.fabX.fabXaccess.device.ws.DeviceToServerCommand
import cloud.fabX.fabXaccess.device.ws.DeviceToServerNotification
import cloud.fabX.fabXaccess.device.ws.GetConfiguration
import cloud.fabX.fabXaccess.device.ws.PinStatusNotification
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class DeviceMetricsIntegrationTest {

    @Test
    fun `given no connected devices when getting device metrics then all devices disconnected`() = withTestApp {
        // given
        val deviceName1 = "A Device 1"
        val deviceName2 = "A Device 2"
        val deviceName3 = "A Device 3"

        val deviceId1 = givenDevice(mac = "AABBCCDDEE01", name = deviceName1)
        val deviceId2 = givenDevice(mac = "AABBCCDDEE02", name = deviceName2)
        val deviceId3 = givenDevice(mac = "AABBCCDDEE03", name = deviceName3)

        // when
        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).all {
            contains("fabx_devices_connected{deviceId=\"${deviceId1}\",deviceName=\"${deviceName1}\"} 0.0")
            contains("fabx_devices_connected{deviceId=\"${deviceId2}\",deviceName=\"${deviceName2}\"} 0.0")
            contains("fabx_devices_connected{deviceId=\"${deviceId3}\",deviceName=\"${deviceName3}\"} 0.0")
        }
    }

    @Test
    fun `given one connected device when getting device metrics then one device connected`() = withTestApp {
        // given
        val mac = "AABBCCDDEE01"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceName1 = "Some Device Name"
        val deviceId1 = givenDevice(mac = mac, secret = secret, name = deviceName1)
        val deviceName2 = "Another Device Name"
        val deviceId2 = givenDevice(mac = "AABBCCDDEE02", name = deviceName2)

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
                contains("fabx_devices_connected{deviceId=\"${deviceId1}\",deviceName=\"${deviceName1}\"} 1.0")
                contains("fabx_devices_connected{deviceId=\"${deviceId2}\",deviceName=\"${deviceName2}\"} 0.0")
            }
        }
    }

    @Test
    fun `when device gets configuration then counter is increased`() = withTestApp {
        // given
        val mac = "AABBCCDDEE01"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceName = "Some Device Name"
        val deviceId = givenDevice(mac = mac, secret = secret, name = deviceName)

        val commandId1 = 4242
        val command1 = GetConfiguration(commandId1, "1.42.2")

        val commandId2 = 123
        val command2 = GetConfiguration(commandId2, "1.42.2")

        // when
        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command1)))
            val responseText1 = (incoming.receive() as Frame.Text).readText()
            val response1 = Json.decodeFromString<DeviceResponse>(responseText1)
            assertThat(response1.commandId).isEqualTo(commandId1)

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerCommand>(command2)))
            val responseText2 = (incoming.receive() as Frame.Text).readText()
            val response2 = Json.decodeFromString<DeviceResponse>(responseText2)
            assertThat(response2.commandId).isEqualTo(commandId2)
        }

        // then
        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).contains(
            "fabx_device_get_configuration_count_total{deviceId=\"$deviceId\",deviceName=\"$deviceName\"} 2.0"
        )
    }

    @Test
    fun `when getting device metrics then returns device pin status`() = withTestApp {
        // given
        val mac = "AABBCCDDEE01"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceName1 = "Some Device Name"
        val deviceId1 = givenDevice(mac = mac, secret = secret, name = deviceName1)

        val notification = PinStatusNotification(
            mapOf(
                1 to true,
                2 to false,
                3 to true,
                4 to false,
                5 to false,
                6 to false,
                7 to false,
                8 to false
            )
        )

        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text
            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerNotification>(notification)))

            delay(100.milliseconds)

            // when
            val response = c().get("/metrics") {
                basicAuth("metrics", "supersecretmetricspassword")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            println(response.bodyAsText().split("\n").filter { it.contains("fabx") }.joinToString("\n"))
            assertThat(response.bodyAsText()).all {
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"1\"} 1.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"2\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"3\"} 1.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"4\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"5\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"6\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"7\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"8\"} 0.0")
            }
        }
    }

    @Test
    fun `given updated pin status when getting device metrics then returns updated device pin status`() = withTestApp {
        // given
        val mac = "AABBCCDDEE01"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceName1 = "Some Device Name"
        val deviceId1 = givenDevice(mac = mac, secret = secret, name = deviceName1)

        val notification1 = PinStatusNotification(
            mapOf(
                1 to true,
                2 to false,
                3 to true,
                4 to false,
                5 to false,
                6 to false,
                7 to false,
                8 to false
            )
        )

        val notification2 = PinStatusNotification(
            mapOf(
                1 to false,
                2 to true,
                3 to false,
                4 to false,
                5 to false,
                6 to false,
                7 to false,
                8 to true
            )
        )

        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text
            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerNotification>(notification1)))
            delay(100.milliseconds)
            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerNotification>(notification2)))
            delay(100.milliseconds)

            // when
            val response = c().get("/metrics") {
                basicAuth("metrics", "supersecretmetricspassword")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.bodyAsText()).all {
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"1\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"2\"} 1.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"3\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"4\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"5\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"6\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"7\"} 0.0")
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"8\"} 1.0")
            }
        }
    }

    @Test
    fun `given device disconnected when getting device metrics then no longer returns device pin status`() = withTestApp {
        // given
        val mac = "AABBCCDDEE01"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceName1 = "Some Device Name"
        val deviceId1 = givenDevice(mac = mac, secret = secret, name = deviceName1)

        val notification = PinStatusNotification(mapOf(1 to true))

        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text
            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerNotification>(notification)))

            delay(100.milliseconds)

            // when
            val initialResponse = c().get("/metrics") {
                basicAuth("metrics", "supersecretmetricspassword")
            }

            // then
            assertThat(initialResponse.status).isEqualTo(HttpStatusCode.OK)
            assertThat(initialResponse.bodyAsText()).all {
                contains("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"1\"} 1.0")
            }
        }

        delay(100.milliseconds)

        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }

        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).all {
            doesNotContain("fabx_device_pins{deviceId=\"$deviceId1\",pin=\"1\"} 1.0")
        }
    }
}