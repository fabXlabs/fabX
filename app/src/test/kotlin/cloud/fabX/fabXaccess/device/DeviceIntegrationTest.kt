package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.rest.Device
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class DeviceIntegrationTest {

    @Test
    fun `given no authentication when get devices then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given invalid authentication whe get devices then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device") {
            addBasicAuth("no.body", "secret123")
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(result.response.content).isNull()
    }

    @Test
    fun `given devices when get devices then returns devices`() = withTestApp {
        // given
        // TODO givenDevice("device1"), 2, 3

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Device>>()
            .isEmpty() // TODO isNotEmpty
    }

    @Disabled // TODO re-enable once givenDevice is implemented
    @Test
    fun `given device when get device by id then returns device`() = withTestApp {
        // given
        val deviceId = TODO("givenDevice")

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device/") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Device>()
            .transform { it.id }
            .isEqualTo(deviceId)
    }

    @Test
    fun `given device does not exist when get device by id then returns http not found`() = withTestApp {
        // given
        val invalidDeviceId = DeviceIdFixture.arbitrary().serialize()

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device/$invalidDeviceId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "Device with id DeviceId(value=$invalidDeviceId) not found.",
                    mapOf("deviceId" to invalidDeviceId)
                )
            )
    }
}