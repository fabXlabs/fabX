package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.addMemberAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.rest.Device
import cloud.fabX.fabXaccess.device.rest.DeviceCreationDetails
import cloud.fabX.fabXaccess.device.rest.DeviceDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    fun `given invalid authentication when get devices then returns http unauthorized`() = withTestApp {
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
    fun `given non-admin authentication when get devices then returns http forbidden`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin.",
                    mapOf()
                )
            )
    }

    @Test
    fun `given devices when get devices then returns devices`() = withTestApp {
        // given
        val deviceId1 = givenDevice("device1", mac = "aabbccaabb01")
        val deviceId2 = givenDevice("device2", mac = "aabbccaabb02")
        val deviceId3 = givenDevice("device3", mac = "aabbccaabb03")

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Device>>()
            .transform { devices -> devices.map { it.id } }
            .containsExactlyInAnyOrder(deviceId1, deviceId2, deviceId3)
    }

    @Test
    fun `given device when get device by id then returns device`() = withTestApp {
        // given
        val deviceId = givenDevice("newDevice", mac = "001122334455")

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/device/$deviceId") {
            addAdminAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Device>()
            .isEqualTo(
                Device(
                    deviceId,
                    1,
                    "newDevice",
                    "https://example.com/bg.bmp",
                    "https://backup.example.com",
                    mapOf()
                )
            )
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

    @Test
    fun `given non-admin authentication when adding device then returns http forbidden`() = withTestApp {
        // given
        val requestBody = DeviceCreationDetails(
            "device42",
            "https://example.com/bg42.bmp",
            "https://backup.example.com",
            "aabbccddeeff",
            "supersecret"
        )

        // when

        val result = handleRequest(HttpMethod.Post, "/api/v1/device") {
            addMemberAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given device when changing device then returns http ok`() = withTestApp {
        // given
        val deviceId = givenDevice(mac = "aa00bb11cc22")

        val requestBody = DeviceDetails(
            ChangeableValue("newName"),
            ChangeableValue("https://example.com/newbg.bmp"),
            null
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/device/$deviceId") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content).isNull()

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
                    "newName",
                    "https://example.com/newbg.bmp",
                    "https://backup.example.com",
                    mapOf()
                )
            )
    }

    @Test
    fun `given invalid device when changing device then returns http not found`() = withTestApp {
        // given
        val invalidDeviceId = DeviceIdFixture.arbitrary().serialize()

        val requestBody = DeviceDetails(
            null,
            null,
            null
        )

        // when
        val result = handleRequest(HttpMethod.Put, "/api/v1/device/$invalidDeviceId") {
            addAdminAuth()
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(requestBody))
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.NotFound)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Error>()
            .isEqualTo(
                Error(
                    "Device with id DeviceId(value=$invalidDeviceId) not found.",
                    mapOf(
                        "deviceId" to invalidDeviceId
                    )
                )
            )
    }
}