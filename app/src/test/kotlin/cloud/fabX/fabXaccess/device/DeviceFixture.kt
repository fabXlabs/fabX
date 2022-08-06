package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.device.rest.DeviceCreationDetails
import cloud.fabX.fabXaccess.device.rest.ToolAttachmentDetails
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder

internal suspend fun ApplicationTestBuilder.givenDevice(
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

    val response = c().post("/api/v1/device") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    return response.bodyAsText()
}

internal suspend fun ApplicationTestBuilder.givenToolAttachedToDevice(
    deviceId: String,
    pin: Int,
    toolId: String
) {
    val requestBody = ToolAttachmentDetails(toolId)

    // when
    val response = c().put("/api/v1/device/$deviceId/attached-tool/$pin") {
        adminAuth()
        contentType(ContentType.Application.Json)
        setBody(requestBody)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
}