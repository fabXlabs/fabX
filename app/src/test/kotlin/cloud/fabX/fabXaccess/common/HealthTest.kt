package cloud.fabX.fabXaccess.common

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

internal class HealthTest {

    @Test
    fun `when getting health then returns http ok`() = withTestApp {
        // given

        // when
        val response = c().get("/health")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.bodyAsText()).isEmpty()
    }
}