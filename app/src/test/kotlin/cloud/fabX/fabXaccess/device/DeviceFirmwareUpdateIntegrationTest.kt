package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import java.io.File
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DeviceFirmwareUpdateIntegrationTest {

    @Test
    fun `given no authentication when firmware update then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/device/me/firmware-update")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given invalid authentication when firmware update then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/device/me/firmware-update") {
            basicAuth("no.body", "secret123")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given non-device authentication when firmware update then returns http unauthorized`() = withTestApp {
        // given

        // when
        val response = c().get("/api/v1/device/me/firmware-update") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given actual firmware is desired firmware when firmware update then returns http no content`() = withTestApp {
        // given
        val firmwareVersion = "1.42.0"

        val mac = "AABBCCDDEEFF"
        val secret = "abcdef0123456789abcdef0123456789"
        val deviceId = givenDevice(mac = mac, secret = secret)
        givenDesiredFirmwareVersion(deviceId, firmwareVersion)

        // when
        val response = c().get("/api/v1/device/me/firmware-update") {
            basicAuth(mac, secret)
            header("X-ESP32-Version", firmwareVersion)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Nested
    internal inner class GivenFirmwareFile {

        private fun withSetupTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp {
            val firmwareDirectory = File("/tmp/fabXIntegrationTest")
            firmwareDirectory.mkdir()

            val firmwareFile = File(firmwareDirectory, "1.42.0.bin")
            firmwareFile.writeBytes("hello world".toByteArray())

            block(this)

            firmwareDirectory.delete()
        }

        @Test
        fun `given actual firmware different than desired firmware when firmware update then returns http ok with firmware binary`() =
            withSetupTestApp {
                // given
                val mac = "AABBCCDDEEFF"
                val secret = "abcdef0123456789abcdef0123456789"
                val deviceId = givenDevice(mac = mac, secret = secret)
                givenDesiredFirmwareVersion(deviceId, "1.42.0")

                // when
                val response = c().get("/api/v1/device/me/firmware-update") {
                    basicAuth(mac, secret)
                    header("X-ESP32-Version", "1.43.0")
                }

                // then
                assertThat(response.status).isEqualTo(HttpStatusCode.OK)
                assertThat(response.body<ByteArray>()).isEqualTo("hello world".toByteArray())
            }
    }
}