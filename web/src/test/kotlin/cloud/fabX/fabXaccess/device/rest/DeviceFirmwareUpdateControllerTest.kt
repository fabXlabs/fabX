package cloud.fabX.fabXaccess.device.rest

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.device.application.GettingDevice
import cloud.fabX.fabXaccess.device.model.DeviceFixture
import cloud.fabX.fabXaccess.device.ws.DevicePrincipal
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.rest.AuthenticationService
import cloud.fabX.fabXaccess.user.rest.UserPrincipal
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import java.io.File
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class DeviceFirmwareUpdateControllerTest {
    private lateinit var gettingDevice: GettingDevice
    private lateinit var authenticationService: AuthenticationService

    private val mac = "AABBCCDDEEFF"
    private val secret = "abcdef0123456789abcdef0123456789"

    private val device = DeviceFixture.arbitrary(mac = mac, secret = secret, desiredFirmwareVersion = "1.42.0")

    @BeforeEach
    fun `configure WebModule`(
        @Mock gettingDevice: GettingDevice,
        @Mock authenticationService: AuthenticationService
    ) {
        this.gettingDevice = gettingDevice
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { gettingDevice }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when updating firmware then returns http ok`() = withConfiguredTestApp {
        // given
        whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
            .thenReturn(DevicePrincipal(device))

        whenever(gettingDevice.getMe(eq(device.asActor()), any()))
            .thenReturn(device.right())

        val firmwareDirectory = File("/tmp/fabXtest")
        firmwareDirectory.mkdir()
        val firmwareFile = File(firmwareDirectory, "1.42.0.bin")
        firmwareFile.writeBytes("hello world".toByteArray())

        // when
        val response = c().get("/api/v1/device/me/firmware-update") {
            basicAuth(mac, secret)
            header("X-ESP32-Version", "1.2.3")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<ByteArray>()).isEqualTo("hello world".toByteArray())

        // cleanup
        firmwareDirectory.delete()
    }

    @Test
    fun `given desired firmware is actual firmware when updating firmware then returns http no content`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
                .thenReturn(DevicePrincipal(device))

            whenever(gettingDevice.getMe(eq(device.asActor()), any()))
                .thenReturn(device.right())

            // when
            val response = c().get("/api/v1/device/me/firmware-update") {
                basicAuth(mac, secret)
                header("X-ESP32-Version", "1.42.0")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
            assertThat(response.body<String>()).isEmpty()
        }

    @Test
    fun `given no device authentication when updating firmware then returns http unauthorized`() =
        withConfiguredTestApp {
            // given
            whenever(authenticationService.basic(UserPasswordCredential(mac, secret)))
                .thenReturn(UserPrincipal(UserFixture.arbitrary()))

            // when
            val response = c().get("/api/v1/device/me/firmware-update") {
                basicAuth(mac, secret)
                header("X-ESP32-Version", "1.42.0")
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
}