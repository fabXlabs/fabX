package cloud.fabX.fabXaccess.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.withTestAppB
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class UserIdentityIntegrationTest {

    @Test
    fun `given username password identity when http request then can authenticate via http basic auth`() =
        withTestAppB {
            // given
            val userId = givenUser()
            val username = "username123"
            val password = "password123"
            givenUsernamePasswordIdentity(userId, username, password)

            // when
            val response = c().get("/api/v1/tool") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        }

    @Test
    fun `given removed username password identity when http request then no longer can authenticate via http basic auth`() =
        withTestAppB {
            // given
            val userId = givenUser()
            val username = "username123"
            val password = "password123"
            givenUsernamePasswordIdentity(userId, username, password)

            val removeResponse = c().delete("/api/v1/user/$userId/identity/username-password/$username") {
                adminAuth()
            }
            assertThat(removeResponse.status).isEqualTo(HttpStatusCode.NoContent)

            // when
            val response = c().get("/api/v1/tool") {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        }
}