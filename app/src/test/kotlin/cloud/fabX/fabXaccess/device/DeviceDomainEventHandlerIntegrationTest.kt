package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.rest.Device
import cloud.fabX.fabXaccess.tool.givenTool
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

internal class DeviceDomainEventHandlerIntegrationTest {

    @Test
    fun `when deleting tool then it is detached from device`() = withTestApp {
        // given
        val deviceId = givenDevice(mac = "AABBCCDDEEFF")
        val toolId = givenTool()
        givenToolAttachedToDevice(deviceId, 0, toolId)

        // when
        val response = c().delete("/api/v1/tool/$toolId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)

        val responseGet = c().get("/api/v1/device/$deviceId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<Device>())
            .isEqualTo(
                Device(
                    deviceId,
                    3,
                    "device",
                    "https://example.com/bg.bmp",
                    "https://backup.example.com",
                    null,
                    null,
                    mapOf()
                )
            )
    }
}