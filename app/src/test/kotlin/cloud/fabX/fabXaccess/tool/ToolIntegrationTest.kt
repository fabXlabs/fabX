package cloud.fabX.fabXaccess.tool

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.addMemberAuth
import cloud.fabX.fabXaccess.common.isJson
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.tool.rest.Tool
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
class ToolIntegrationTest {

    @Test
    fun `given no authentication when get tools then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool")

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given invalid authentication when get tools then returns http unauthorized`() = withTestApp {
        // given

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addBasicAuth("no.body", "s3cr3t")
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
    }

    @Test
    fun `given tools when get tools then returns tools`() = withTestApp {
        // given
        // TODO given tools (once we can create tools via api)

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addMemberAuth()
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
        assertThat(result.response.content)
            .isNotNull()
            .isJson<Set<Tool>>()
            .isEmpty()
    }

    // TODO given tool when get tool by id then returns tool
}