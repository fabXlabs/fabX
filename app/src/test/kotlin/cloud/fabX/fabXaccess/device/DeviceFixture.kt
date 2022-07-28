package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.device.rest.DeviceCreationDetails
import cloud.fabX.fabXaccess.device.rest.ToolAttachmentDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@InternalAPI
@ExperimentalSerializationApi
internal fun TestApplicationEngine.givenDevice(
    name: String = "device",
    background: String = "https://example.com/bg.bmp",
    backupBackendUrl: String = "https://backup.example.com",
    mac: String,
    secret: String = "supersecret123"
): String {
    val requestBody = DeviceCreationDetails(
        name,
        background,
        backupBackendUrl,
        mac,
        secret
    )

    val result = handleRequest(HttpMethod.Post, "/api/v1/device") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
    return result.response.content!!
}

@InternalAPI
@ExperimentalSerializationApi
internal fun TestApplicationEngine.givenToolAttachedToDevice(
    deviceId: String,
    pin: Int,
    toolId: String
) {
    val requestBody = ToolAttachmentDetails(toolId)

    // when
    val result = handleRequest(HttpMethod.Put, "/api/v1/device/$deviceId/attached-tool/$pin") {
        addAdminAuth()
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.encodeToString(requestBody))
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.NoContent)
}