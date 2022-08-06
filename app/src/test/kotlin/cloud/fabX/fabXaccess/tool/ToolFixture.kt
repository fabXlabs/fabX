package cloud.fabX.fabXaccess.tool

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolCreationDetails
import cloud.fabX.fabXaccess.tool.rest.ToolType
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal suspend fun ApplicationTestBuilder.givenTool(
    name: String = "tool",
    type: ToolType = ToolType.UNLOCK,
    time: Int = 10_000,
    idleState: IdleState = IdleState.IDLE_HIGH,
    wikiLink: String = "https://example.com/tool",
    requiredQualifications: Set<String> = setOf()
): String {
    val requestBody = ToolCreationDetails(
        name,
        type,
        time,
        idleState,
        wikiLink,
        requiredQualifications
    )

    val response = c().post("/api/v1/tool") {
        adminAuth()
        setBody(requestBody)
        contentType(ContentType.Application.Json)
    }

    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
    return response.bodyAsText()
}

internal fun TestApplicationEngine.givenTool(
    name: String = "tool",
    type: ToolType = ToolType.UNLOCK,
    time: Int = 10_000,
    idleState: IdleState = IdleState.IDLE_HIGH,
    wikiLink: String = "https://example.com/tool",
    requiredQualifications: Set<String> = setOf()
): String {
    val requestBody = ToolCreationDetails(
        name,
        type,
        time,
        idleState,
        wikiLink,
        requiredQualifications
    )

    val result = handleRequest(HttpMethod.Post, "/api/v1/tool") {
        addAdminAuth()
        setBody(Json.encodeToString(requestBody))
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }

    assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
    return result.response.content!!
}