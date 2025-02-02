package cloud.fabX.fabXaccess.user.rest

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.withTestApp
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.jupiter.api.Test
import org.kodein.di.bindConstant
import org.mockito.junit.jupiter.MockitoSettings

@MockitoSettings
internal class LogoutControllerTest {

    private val cookieDomain = "example.com"
    private val cookiePath = "/"

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindConstant(tag = "cookieDomain", overrides = true) { cookieDomain }
        bindConstant(tag = "cookiePath", overrides = true) { cookiePath }
    }, block)

    @Test
    fun `when logout then removes cookie`() =
        withConfiguredTestApp {
            // given

            // when
            val response = c().get("/api/v1/logout")

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
            assertThat(response.headers[HttpHeaders.SetCookie]!!)
                .all {
                    transform { it.split(";")[0].split("=") }
                        .all {
                            transform { it[0] }.isEqualTo("FABX_AUTH")
                            transform { it[1] }.isEmpty()
                        }
                    transform { it.split(";").map { it.trim() } }.all {
                        contains("Secure")
                        contains("HttpOnly")
                        contains("Domain=example.com")
                        contains("Path=/")
                    }
                }
        }
}