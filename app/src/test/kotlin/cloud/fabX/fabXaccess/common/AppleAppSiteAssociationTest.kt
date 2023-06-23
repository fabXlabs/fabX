package cloud.fabX.fabXaccess.common

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

internal class AppleAppSiteAssociationTest {

    @Test
    fun `when getting apple app site association then returns http ok`() = withTestApp {
        // given

        // when
        val response = c().get("/.well-known/apple-app-site-association")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).isEqualTo("{\"webcredentials\":{\"apps\":[\"7V5294RF62.cloud.fabx.fabX\"]}}")
    }
}