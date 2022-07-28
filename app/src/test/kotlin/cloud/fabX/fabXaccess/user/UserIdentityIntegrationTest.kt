package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEqualTo
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
    fun `when adding username password identity then can authenticate via http basic auth`() = withTestApp {
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
}