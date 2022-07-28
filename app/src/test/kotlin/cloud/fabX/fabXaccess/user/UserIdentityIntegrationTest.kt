package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.addAdminAuth
import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.withTestApp
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class UserIdentityIntegrationTest {

    @Test
    fun `given username password identity when http request then can authenticate via http basic auth`() = withTestApp {
        // given
        val userId = givenUser()
        val username = "username123"
        val password = "password123"
        givenUsernamePasswordIdentity(userId, username, password)

        // when
        val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
            addBasicAuth(username, password)
        }

        // then
        assertThat(result.response.status()).isEqualTo(HttpStatusCode.OK)
    }

    @Test
    fun `given removed username password identity when http request then no longer can authenticate via http basic auth`() =
        withTestApp {
            // given
            val userId = givenUser()
            val username = "username123"
            val password = "password123"
            givenUsernamePasswordIdentity(userId, username, password)

            val removeResult = handleRequest(
                HttpMethod.Delete,
                "/api/v1/user/$userId/identity/username-password/$username"
            ) {
                addAdminAuth()
            }
            assertThat(removeResult.response.status()).isEqualTo(HttpStatusCode.OK)

            // when
            val result = handleRequest(HttpMethod.Get, "/api/v1/tool") {
                addBasicAuth(username, password)
            }

            // then
            assertThat(result.response.status()).isEqualTo(HttpStatusCode.Unauthorized)
        }
}