package cloud.fabX.fabXaccess.tool

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.tool.rest.IdleState
import cloud.fabX.fabXaccess.tool.rest.ToolCreationDetails
import cloud.fabX.fabXaccess.tool.rest.ToolType
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