package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.rest.Device
import cloud.fabX.fabXaccess.tool.givenTool
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class DeviceDomainEventHandlerIntegrationTest {

    @Test
    fun `when deleting tool then it is detached from device`() = withTestApp {
        // given
        val deviceId = givenDevice(mac = "aabbccddeeff")
        val toolId = givenTool()
        givenToolAttachedToDevice(deviceId, 0, toolId)

        // when
        val result = handleRequest(HttpMethod.Delete, "/api/v1/tool/$toolId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)

        val resultGet = handleRequest(HttpMethod.Get, "/api/v1/device/$deviceId") {
            addAdminAuth()
        }
        assertThat(resultGet.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(resultGet.response.content)
            .isNotNull()
            .isJson<Device>()
            .isEqualTo(
                Device(
                    deviceId,
                    2,
                    "device",
                    "https://example.com/bg.bmp",
                    "https://backup.example.com",
                    mapOf()
                )
            )
    }
}