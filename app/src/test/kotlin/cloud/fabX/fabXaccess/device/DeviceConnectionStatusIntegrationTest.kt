package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isError
import cloud.fabX.fabXaccess.common.memberAuth
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlin.test.Test

internal class DeviceConnectionStatusIntegrationTest {
    @Test
    fun `given no authentication when getting device connection status then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/device/connection-status")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given invalid authentication when getting device connection status then returns http unauthorized`() =
        withTestApp {
            // given

            // when
            val response = c().get("/api/v1/device/connection-status") {
                basicAuth("no.body", "secret123")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
        }

    @Test
    fun `when getting device connection status then returns status`() = withTestApp {
        // given
        val mac1 = "AABB11CC22DD"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val mac2 = "AABB11CC22EE"

        val deviceId1 = givenDevice(mac = mac1, secret = secret)
        val deviceId2 = givenDevice(mac = mac2)

        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac1, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            // when
            val response = c().get("/api/v1/device/connection-status") {
                memberAuth()
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<Map<String, Boolean>>())
                .containsOnly(
                    deviceId1 to true,
                    deviceId2 to false
                )
        }
    }

    @Test
    fun `given no authentication when getting device connection status by id then returns http unauthorized`() =
        withTestApp {
            // given
            val mac = "AABB11CC22DD"
            val deviceId = givenDevice(mac = mac)

            // when
            val response = c().get("/api/v1/device/${deviceId}/connection-status")

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
        }

    @Test
    fun `given invalid authentication when getting device connection status by id then returns http unauthorized`() =
        withTestApp {
            // given
            val mac = "AABB11CC22DD"
            val deviceId = givenDevice(mac = mac)

            // when
            val response = c().get("/api/v1/device/${deviceId}/connection-status") {
                basicAuth("no.body", "secret123")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            assertThat(response.bodyAsText()).isEmpty()
        }

    @Test
    fun `when getting device connection status by id then returns status`() = withTestApp {
        // given
        val mac = "AABB11CC22DD"
        val secret = "49ecad93aac0bdff2915768bd514678f"

        val deviceId = givenDevice(mac = mac, secret = secret)

        c().webSocket("/api/v1/device/ws", {
            basicAuth(mac, secret)
        }) {
            (incoming.receive() as Frame.Text).readText() // greeting text

            // when
            val response = c().get("/api/v1/device/${deviceId}/connection-status") {
                memberAuth()
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(response.body<Boolean>())
                .isEqualTo(true)
        }
    }

    @Test
    fun `given unknown device when getting device connection status by id then returns http not found`() = withTestApp {
        // given
        val invalidDeviceId = DeviceIdFixture.arbitrary().serialize()

        // when
        val response = c().get("/api/v1/device/${invalidDeviceId}/connection-status") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isError(
                "DeviceNotFound",
                "Device with id DeviceId(value=$invalidDeviceId) not found.",
                mapOf("deviceId" to invalidDeviceId)
            )
    }
}
