package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestApp
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

class UserMetricsIntegrationTest {

    @Test
    fun `given test users when getting user metrics then returns 2 user amount`() = withTestApp {
        // given

        // when
        val response = c().get("/metrics") {
            basicAuth("metrics", "supersecretmetricspassword")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).contains(
            "fabx_users_amount 2.0"
        )
    }
}